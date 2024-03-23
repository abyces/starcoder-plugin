package com.intellij.smartcoder.settings;

import com.intellij.smartcoder.domain.models.PromptModel;

public interface BaseModelSettings {
    String getApiURL();

    String getApiToken();

    float getTemperature();

    int getMaxTokens();

    float getTopP();

    float getFrequencyPenalty();

    float getPresencePenalty();

    String getModel();

    PromptModel getFimTokenModel();

}
