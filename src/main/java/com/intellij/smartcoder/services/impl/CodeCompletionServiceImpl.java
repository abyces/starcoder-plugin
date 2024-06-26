package com.intellij.smartcoder.services.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.google.common.collect.Lists;
import com.intellij.openapi.editor.Editor;
import com.intellij.smartcoder.domain.enums.CompletionType;
import com.intellij.smartcoder.services.CodeAnalyseService;
import com.intellij.smartcoder.services.CodeCompletionService;
import com.intellij.smartcoder.settings.BaseModelSettings;
import com.intellij.smartcoder.settings.impl.DeepSeekSettings;
import com.intellij.smartcoder.utils.OkHttpUtil;
import com.intellij.smartcoder.utils.PropertyUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeCompletionServiceImpl implements CodeCompletionService {
    private final BaseModelSettings modelSetting;
    private final CodeAnalyseService codeAnalyseService;
    private int statusCode = 200;
    private final Pattern pattern = Pattern.compile("```(?:java|js|python|javascript)?\\n([\\s\\S]*?)```");

    public CodeCompletionServiceImpl() {
        this.codeAnalyseService = new CodeAnalyseServiceImpl();
        this.modelSetting = DeepSeekSettings.getInstance();
    }

    @Override
    public String[] getCompletionHints(Editor focusedEditor, int cursorPosition) {
        CharSequence editorContents = focusedEditor.getDocument().getCharsSequence();
        String prompt = modelSetting.getFimTokenModel().generateFIMPrompt("", editorContents.toString(), cursorPosition);
        if(StringUtils.isBlank(prompt)) {
            return null;
        }

        System.out.println(JSON.toJSONString(prompt));
        CompletionType completionType = codeAnalyseService.preProcessEditorContent(focusedEditor, editorContents, cursorPosition);
        String generatedText = getCompletionResponse(prompt, completionType);
        System.out.println(generatedText);
        return Objects.requireNonNull(buildSuggestionList(generatedText)).toArray(new String[] {});
    }

    /**
     * 获取模型补全列表
     *
     * @param focusedEditor
     * @param cursorPosition
     * @return
     */
    @Override
    public String[] getCodeCompletionHints(Editor focusedEditor, int cursorPosition) {
        CharSequence editorContents = focusedEditor.getDocument().getCharsSequence();
        String prompt = modelSetting.getFimTokenModel().generateChatCompletionPrompt(modelSetting.getModel(), editorContents.toString(), cursorPosition);
        if(StringUtils.isBlank(prompt)) {
            return null;
        }

        List<Map<String, Object>> messages = Lists.newArrayList(
                new HashMap<>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        );
        System.out.println(JSON.toJSONString(messages));
        CompletionType completionType = codeAnalyseService.preProcessEditorContent(focusedEditor, editorContents, cursorPosition);
        String generatedText = getChatCompletionResponse(messages, completionType);
        System.out.println(generatedText);
        return Objects.requireNonNull(buildSuggestionList(generatedText)).toArray(new String[] {});
    }

    public String replacementSuggestion (String selectedCode) {
        return selectedCode;
    }

    /**
     *
     *
     * @param messages
     * @param completionType
     * @return
     */
    private String getChatCompletionResponse(List<Map<String, Object>> messages, CompletionType completionType) {
        String apiURL = modelSetting.getApiURL();
        String bearerToken = PropertyUtil.getProperty("ds.token");

        Map<String, Object> httpBody = new HashMap<>();
        httpBody.put("model", modelSetting.getModel());
        httpBody.put("temperature", modelSetting.getTemperature());
        httpBody.put("max_tokens", modelSetting.getMaxTokens());
        httpBody.put("top_p", modelSetting.getTopP());
        httpBody.put("frequency_penalty", modelSetting.getFrequencyPenalty());
        httpBody.put("presence_penalty", modelSetting.getPresencePenalty());

        httpBody.put("messages", messages);
        if (CompletionType.ONE_LINE == completionType) {
            httpBody.put("stop", List.of("\n"));
        }
        String response = OkHttpUtil.post(apiURL, JSON.toJSONString(httpBody), bearerToken);
        if (StringUtils.isBlank(response)) {
            return "";
        }
        return String.valueOf(JSONPath.extract(response, "$.choices[0].message.content"));
    }

    private String getCompletionResponse(String prompt, CompletionType completionType) {
        String apiURL = modelSetting.getApiURL();

        Map<String, Object> httpBody = new HashMap<>();
        httpBody.put("model", modelSetting.getModel());
        httpBody.put("temperature", modelSetting.getTemperature());
        httpBody.put("max_tokens", modelSetting.getMaxTokens());
        httpBody.put("top_p", modelSetting.getTopP());
        httpBody.put("frequency_penalty", modelSetting.getFrequencyPenalty());
        httpBody.put("presence_penalty", modelSetting.getPresencePenalty());
        httpBody.put("stream", modelSetting.isStream());

        httpBody.put("prompt", prompt);
        if (CompletionType.ONE_LINE == completionType) {
            httpBody.put("stop", List.of("\n"));
        }

        String response = OkHttpUtil.post(apiURL, JSON.toJSONString(httpBody));
        if (StringUtils.isBlank(response)) {
            return "";
        }
        return String.valueOf(JSONPath.extract(response, "$.response"));


    }

    /**
     *
     * @param generatedText
     * @return
     */
    private List<String> buildSuggestionList(String generatedText) {
        System.out.println("Generated Text: " + generatedText);
        List<String> suggestionList = Lists.newArrayList();
        Matcher matcher = pattern.matcher(generatedText);
        if (matcher.find()) {
            suggestionList.add(matcher.group(1));
        } else {
            suggestionList.add(generatedText);
        }
        System.out.println("Matched Results: " + JSON.toJSONString(suggestionList));
        return suggestionList;
    }

    /**
     *
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }


}
