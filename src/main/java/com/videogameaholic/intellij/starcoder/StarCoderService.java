package com.videogameaholic.intellij.starcoder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.videogameaholic.intellij.starcoder.domain.dto.ChatCompletionResponse;
import com.videogameaholic.intellij.starcoder.domain.dto.ResponseChoice;
import com.videogameaholic.intellij.starcoder.domain.dto.ResponseTokenUsage;
import com.videogameaholic.intellij.starcoder.domain.enums.PromptModel;
import com.videogameaholic.intellij.starcoder.settings.BaseModelSettings;
import com.videogameaholic.intellij.starcoder.settings.Property;
import com.videogameaholic.intellij.starcoder.settings.impl.DeepSeekSettings;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
public class StarCoderService {

    private Property property;

    private int statusCode = 200;

    public StarCoderService() {
        property = new Property();
    }

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        DeepSeekSettings settings = DeepSeekSettings.getInstance();

        PromptModel fimModel = settings.getFimTokenModel();
        String prompt = fimModel.generateChatCompletionPrompt("",editorContents.toString(), cursorPosition);
        prompt = settings.getMessages().replace("<PLACEHOLDER>", prompt);
        System.out.println("ChatCompletion Prompt: {}".replace("{}", prompt));
        if(prompt.isEmpty()) return null;

        HttpPost httpPost = buildApiPost(settings, prompt);
        String generatedText = getApiResponse(httpPost);
        System.out.println(String.format("ChatCompletion Prompt: {}", generatedText));
        return fimModel.buildSuggestionList(generatedText);
    }

    private HttpPost buildApiPost (BaseModelSettings settings, String prompt) {
        String apiURL = settings.getApiURL();
        String bearerToken = settings.getApiToken();
        float temperature = settings.getTemperature();
        int maxTokens = settings.getMaxTokens();
        float topP = settings.getTopP();
        float frequencyPenalty = settings.getFrequencyPenalty();
        float presencePenalty = settings.getPresencePenalty();
        String model = settings.getModel();

        HttpPost httpPost = new HttpPost(apiURL);
        if(!bearerToken.isBlank() || bearerToken.isEmpty()) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + property.getProperty("ds.token"));
        }

        Map<String, Object> httpBody = new HashMap<>();
        httpBody.put("model", model);
        httpBody.put("messages", JSON.parseArray(prompt, Map.class));
        httpBody.put("temperature", temperature);
        httpBody.put("max_tokens", maxTokens);
        httpBody.put("top_p", topP);
        httpBody.put("frequency_penalty", frequencyPenalty);
        httpBody.put("presence_penalty", presencePenalty);

        StringEntity requestEntity = new StringEntity(JSON.toJSONString(httpBody), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        return httpPost;
    }

    private String getApiResponse(HttpPost httpPost) {
        String responseText = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);

            // Check the response status code
            int oldStatusCode = statusCode;
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != oldStatusCode) {
                // Update the widget based on the new status code
                for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
                    WindowManager.getInstance().getStatusBar(openProject).updateWidget(StarCoderWidget.ID);
                }
            }
            if (statusCode != 200) {
                return responseText;
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            ChatCompletionResponse chatCompletionResponse = parseChatCompletionResponse(responseBody);
            responseText = extractGeneratedText(chatCompletionResponse.getChoices()).orElse("");

            httpClient.close();
        } catch (IOException e) {
            // TODO log exception
        }
        return responseText;
    }

    private ChatCompletionResponse parseChatCompletionResponse(String responseBody) throws IOException {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        ChatCompletionResponse response = new ChatCompletionResponse();
        response.setId(jsonObject.getString("id"));
        response.setModel(jsonObject.getString("model"));
        response.setObject(jsonObject.getString("object"));
        response.setCreated(jsonObject.getLong("created"));
        response.setTokenUsage(jsonObject.getObject("usage", ResponseTokenUsage.class));
        response.setChoices(JSON.parseArray(jsonObject.getString("choices"), ResponseChoice.class));
        return response;
    }

    private Optional<String> extractGeneratedText(List<ResponseChoice> choices) {
        if (Objects.isNull(choices) || choices.isEmpty()) {
            throw new IllegalArgumentException("Invalid response body missing \\'choices\\' key");
        }

        return Optional.of(choices.get(0).getMessage().getContent());
    }

    public String replacementSuggestion (String prompt) {
        // Default to returning the same text.
        String replacement = prompt;

        DeepSeekSettings settings = DeepSeekSettings.getInstance();
        HttpPost httpPost = buildApiPost(settings, prompt);
        String generatedText = getApiResponse(httpPost);
        if(!StringUtils.isEmpty(generatedText)) {
            replacement = generatedText;
        }

        return replacement;
    }

    public int getStatus () {
        return statusCode;
    }
}
