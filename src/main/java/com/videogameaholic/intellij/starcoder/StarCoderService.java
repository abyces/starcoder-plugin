package com.videogameaholic.intellij.starcoder;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.TypeReference;
import com.videogameaholic.intellij.starcoder.domain.dto.ChatCompletionResponse;
import com.videogameaholic.intellij.starcoder.domain.dto.ResponseChoice;
import com.videogameaholic.intellij.starcoder.domain.dto.ResponseTokenUsage;
import com.videogameaholic.intellij.starcoder.domain.enums.PromptModel;
import com.videogameaholic.intellij.starcoder.settings.BaseModelSettings;
import com.videogameaholic.intellij.starcoder.settings.Property;
import com.videogameaholic.intellij.starcoder.settings.impl.DeepSeekSettings;
import com.videogameaholic.intellij.starcoder.utils.OkHttpUtil;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;

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

        String generatedText = buildApiPost(settings, prompt);
        System.out.println(generatedText);
        return fimModel.buildSuggestionList(generatedText);
    }

    /**
     * 构建API POST请求
     *
     * @param settings  BaseModelSettings对象，包含API配置
     * @param prompt   提示文本
     * @return response
     */
    public String buildApiPost(BaseModelSettings settings, String prompt) {
        String apiURL = settings.getApiURL();
        String bearerToken = property.getProperty("ds.token");
        List<Map<String, Object>> messages = JSON.parseObject(prompt, new TypeReference<List<Map<String, Object>>>() {});
        Map<String, Object> httpBody = new HashMap<>();
        httpBody.put("model", settings.getModel());
        httpBody.put("messages", messages);
        httpBody.put("temperature", settings.getTemperature());
        httpBody.put("max_tokens", settings.getMaxTokens());
        httpBody.put("top_p", settings.getTopP());
        httpBody.put("frequency_penalty", settings.getFrequencyPenalty());
        httpBody.put("presence_penalty", settings.getPresencePenalty());

        String response = OkHttpUtil.post(apiURL, JSON.toJSONString(httpBody), bearerToken);
        if (StringUtils.isBlank(response)) {
            return "";
        }
        return String.valueOf(JSONPath.extract(response, "$.choices[0].message.content"));
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
        String generatedText = buildApiPost(settings, prompt);
        if(!StringUtils.isEmpty(generatedText)) {
            replacement = generatedText;
        }

        return replacement;
    }

    public int getStatus () {
        return statusCode;
    }
}
