package com.intellij.smartcoder.domain.dto;


import java.util.List;

public class ChatCompletionResponse {
    private String id;

    private String model;

    private String object;

    private Long created;

    private ResponseTokenUsage tokenUsage;

    private List<ResponseChoice> choices;

    public ChatCompletionResponse() {
    }

    public ChatCompletionResponse(String id, String model, String object, Long created, ResponseTokenUsage tokenUsage, List<ResponseChoice> choices) {
        this.id = id;
        this.model = model;
        this.object = object;
        this.created = created;
        this.tokenUsage = tokenUsage;
        this.choices = choices;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public ResponseTokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(ResponseTokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public List<ResponseChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<ResponseChoice> choices) {
        this.choices = choices;
    }
}
