package com.videogameaholic.intellij.starcoder.settings;

public interface BaseModelSettings {
    public String getApiURL();

    public String getApiToken();

    public float getTemperature();

    public int getMaxTokens();

    public float getTopP();

    public float getFrequencyPenalty();

    public float getPresencePenalty();

    public float getRepetitionPenalty();

    public String getModel();

    public String getMessages();
}
