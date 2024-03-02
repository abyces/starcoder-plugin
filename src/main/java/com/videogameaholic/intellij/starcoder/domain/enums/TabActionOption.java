package com.videogameaholic.intellij.starcoder.domain.enums;

public enum TabActionOption {
    // TODO add action class here?
    ALL("All suggestions"),
    SINGLE("Single line at a time"),
    DISABLED("None");

    private String description;

    TabActionOption(String description) {
        this.description = description;
    }

    @Override public String toString() { return description; }
}
