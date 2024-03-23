package com.videogameaholic.intellij.starcoder.domain.models;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum PromptModel {
    STARCODER ("starcoder","StarCoder", "<fim_prefix>","<fim_suffix>","<fim_middle>", "<|endoftext|>"),
    SANTACODER ("santacoder","SantaCoder", "<fim-prefix>","<fim-suffix>","<fim-middle>", "<|endoftext|>"),
    // Whitespace for Code Llama is intentional
    CODELLAMA ("codellama","Code Llama", "<PRE> "," <SUF>"," <MID>", "<EOT>"),
    DEEPSEEK ("deepseek", "DeepSeek-Coder", "<｜fim▁begin｜>", "<｜fim▁end｜>", "<｜fim▁hole｜>", "<|EOT|>");

    private final String id;
    private final String displayName;
    private final String prefixTag;
    private final String suffixTag;
    private final String middleTag;
    private final String endTag;

    private final String regex = "```(?:java|js|python|javascript)?\\n([\\s\\S]*?)```";
    private final Pattern pattern = Pattern.compile(regex);

    private PromptModel(String uniqueId, String name, String prefix, String suffix, String middle, String end)
    {
        id = uniqueId;
        displayName = name;
        prefixTag = prefix;
        suffixTag = suffix;
        middleTag = middle;
        endTag = end;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString() {
        return displayName;
    }

    public static PromptModel fromId(String attributeValue) {
        return Stream.of(PromptModel.values())
                .filter(model -> model.getId().equals(attributeValue))
                .findFirst()
                .orElse(PromptModel.STARCODER);
    }

    public String generateFIMPrompt(String metaData, String code, int fillPosition) {
        // First validate text does not already contain tokens that would confuse the AI.
        // Future: May replace with alternate tokens and switch back after response.
        if(code.contains(prefixTag) || code.contains(suffixTag) || code.contains(middleTag) || code.contains(endTag)) return "";

        String prefix = code.substring(0, fillPosition);
        String suffix = code.substring(fillPosition);
        return metaData + prefixTag + prefix + suffixTag + suffix + middleTag;
    }

    public String generateChatCompletionPrompt(String metaData, String code, int fillPosition) {
        String prefix = code.substring(0, fillPosition);
        String suffix = code.substring(fillPosition);
        return prefix + "<FILL_ME>" + suffix;
    }

    @Nullable
    public String[] buildSuggestionList(String generatedText) {
        System.out.println("Generated Text: " + generatedText);
        List<String> suggestionList = new ArrayList<>();
        Matcher matcher = pattern.matcher(generatedText);
        if (matcher.find()) {
            suggestionList.add(matcher.group(1));
        } else {
            suggestionList.add("");
        }
        System.out.println("Matched Results: " + JSON.toJSONString(suggestionList));
        return suggestionList.toArray(new String[] {});
    }
}
