package com.intellij.smartcoder.domain.dto;

import com.intellij.smartcoder.domain.enums.Role;


public class Message {
    private Role role;
    private String content;

    public Message() {
    }

    public Message(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
