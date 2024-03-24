package com.intellij.smartcoder.domain.models;

import java.util.stream.Stream;

public enum PromptModel {
    STARCODER ("starcoder","StarCoder", "<fim_prefix>","<fim_suffix>","<fim_middle>", "<|endoftext|>", "<PLACEHOLDER>"),
    SANTACODER ("santacoder","SantaCoder", "<fim-prefix>","<fim-suffix>","<fim-middle>", "<|endoftext|>", "<PLACEHOLDER>"),
    // Whitespace for Code Llama is intentional
    CODELLAMA ("codellama","Code Llama", "<PRE> "," <SUF>"," <MID>", "<EOT>", "<PLACEHOLDER>"),
    DEEPSEEK ("deepseek", "DeepSeek-Coder", "<｜fim▁begin｜>", "<｜fim▁end｜>", "<｜fim▁hole｜>", "<|end_of_sentence|>", "Directly replace <FILL_ME> with correct code. Just Code, No explanation, No Other words!\ncode:\n<PLACEHOLDER>")
    ;

    private final String id;
    private final String displayName;
    private final String prefixTag;
    private final String suffixTag;
    private final String middleTag;
    private final String endTag;
    private final String chatCompletionTemplate;

    PromptModel(String uniqueId, String displayName, String prefix, String suffix, String middle, String end, String chatCompletionTemplate)
    {
        this.id = uniqueId;
        this.displayName = displayName;
        this.prefixTag = prefix;
        this.suffixTag = suffix;
        this.middleTag = middle;
        this.endTag = end;
        this.chatCompletionTemplate = chatCompletionTemplate;
    }

    public String getId() {
        return id;
    }

    public String getEndTag() {
        return endTag;
    }

    public static PromptModel fromModel(String modelName) {
        return Stream.of(PromptModel.values())
                .filter(model -> model.id.equals(modelName.toLowerCase()))
                .findFirst()
                .orElse(PromptModel.DEEPSEEK);
    }

    public String generateFIMPrompt(String metaData, String code, int fillPosition) {
        if(code.contains(prefixTag) || code.contains(suffixTag) || code.contains(middleTag) || code.contains(endTag)) return "";
        String prefix = code.substring(0, fillPosition);
        String suffix = code.substring(fillPosition);
        return metaData + prefixTag + prefix + middleTag + suffix + suffixTag;
    }

    public String generateChatCompletionPrompt(String model, String code, int fillPosition) {
        String prefix = code.substring(0, fillPosition);
        String suffix = code.substring(fillPosition);
        String content = prefix + "<FILL_ME>" + suffix;
        return fromModel(model).chatCompletionTemplate.replace("<PLACEHOLDER>", content);
    }
}
