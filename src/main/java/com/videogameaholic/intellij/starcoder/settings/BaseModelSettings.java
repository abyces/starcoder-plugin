package com.videogameaholic.intellij.starcoder.settings;

public interface BaseModelSettings {
    public String getApiURL();

    public String getApiToken();

    public float getTemperature();

    public int getMaxNewTokens();

    public float getTopP();

    public float getRepetitionPenalty();
}
