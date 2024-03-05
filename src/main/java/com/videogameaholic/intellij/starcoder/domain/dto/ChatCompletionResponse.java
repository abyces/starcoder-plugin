package com.videogameaholic.intellij.starcoder.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionResponse {
    private String id;

    private String model;

    private String object;

    private Long created;

    private ResponseTokenUsage tokenUsage;

    private List<ResponseChoice> choices;
}
