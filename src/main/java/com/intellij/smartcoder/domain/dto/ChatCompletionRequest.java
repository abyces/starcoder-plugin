package com.intellij.smartcoder.domain.dto;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;

public class ChatCompletionRequest {
    private List<Message> messages;

    private String model;

    @JSONField(name="max_tokens")
    private int maxTokens;

    private float temperature;

    @JSONField(name="top_p")
    private float topP;

    private List<String> stop;

    @JSONField(name="frequency_penalty")
    private float frequencyPenalty;

    @JSONField(name="presence_penalty")
    private float presence_penalty;

    public ChatCompletionRequest() {
    }

    public ChatCompletionRequest(List<Message> messages, String model, int maxTokens, float temperature, float topP, List<String> stop, float frequencyPenalty, float presence_penalty) {
        this.messages = messages;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.stop = stop;
        this.frequencyPenalty = frequencyPenalty;
        this.presence_penalty = presence_penalty;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTopP() {
        return topP;
    }

    public void setTopP(float topP) {
        this.topP = topP;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public float getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(float frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public float getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(float presence_penalty) {
        this.presence_penalty = presence_penalty;
    }
}