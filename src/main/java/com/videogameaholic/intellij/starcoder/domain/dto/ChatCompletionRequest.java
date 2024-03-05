package com.videogameaholic.intellij.starcoder.domain.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}