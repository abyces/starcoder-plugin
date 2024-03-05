package com.videogameaholic.intellij.starcoder.domain.dto;

import com.videogameaholic.intellij.starcoder.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Role role;
    private String content;
}
