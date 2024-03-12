package com.videogameaholic.intellij.starcoder.domain.enums;

import com.videogameaholic.intellij.starcoder.settings.BaseModelSettings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

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
        return prefix + "<fim_hole>" + suffix;
    }

    @Nullable
    public String[] buildSuggestionList(String generatedText) {
        String[] suggestionList = null;
        generatedText = generatedText.replace(endTag, "");

        return suggestionList;
    }
}
