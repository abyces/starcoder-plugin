package com.videogameaholic.intellij.starcoder;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.videogameaholic.intellij.starcoder.domain.enums.PromptModel;
import com.videogameaholic.intellij.starcoder.settings.BaseModelSettings;
import com.videogameaholic.intellij.starcoder.settings.Property;
import com.videogameaholic.intellij.starcoder.settings.impl.DeepSeekSettings;
import com.videogameaholic.intellij.starcoder.settings.impl.StarCoderSettings;
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
import java.util.Optional;

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
        int maxNewTokens = settings.getMaxTokens();
        float topP = settings.getTopP();
        float frequencyPenalty = settings.getFrequencyPenalty();
        float presencePenalty = settings.getPresencePenalty();
        String model = settings.getModel();

        HttpPost httpPost = new HttpPost(apiURL);
        if(!bearerToken.isBlank() || bearerToken.isEmpty()) {
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + property.getProperty("ds.token"));
        }

        JsonObject httpBody = new JsonObject();
        httpBody.addProperty("model", model);
        httpBody.addProperty("messages", prompt);
        httpBody.addProperty("temperature", temperature);
        httpBody.addProperty("max_tokens", maxNewTokens);
        httpBody.addProperty("top_p", topP);
        httpBody.addProperty("frequency_penalty", frequencyPenalty);
        httpBody.addProperty("presence_penalty", presencePenalty);

        StringEntity requestEntity = new StringEntity(httpBody.toString(), ContentType.APPLICATION_JSON);
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
            JsonObject responseObject = parseResponse(responseBody);
            responseText = extractGeneratedText(responseObject).orElse("");

            httpClient.close();

        } catch (IOException e) {
            // TODO log exception
        }
        return responseText;
    }

    private JsonObject parseResponse(String responseBody) throws IOException {
        Gson gson = new Gson();
        JsonArray responseArray;

        try {
            responseArray = gson.fromJson(responseBody, JsonArray.class);
            if (responseArray.size() > 0) {
                return responseArray.get(0).getAsJsonObject();
            }
        } catch (JsonSyntaxException ignored) {
            // Fallback.  Response may be an object rather than an array.
            return gson.fromJson(responseBody, JsonObject.class);
        }

        throw new IllegalArgumentException("Response has empty body array");
    }

    private Optional<String> extractGeneratedText(JsonObject responseObject) {
        if (responseObject.get("generated_text") != null) {
            return Optional.of(responseObject.get("generated_text").getAsString());
        }
        throw new IllegalArgumentException("Invalid response body missing \\'generated_text\\' key");
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
