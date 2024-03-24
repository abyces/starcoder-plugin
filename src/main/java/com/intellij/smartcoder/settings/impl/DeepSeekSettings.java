package com.intellij.smartcoder.settings.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.smartcoder.domain.enums.TabActionOption;
import com.intellij.smartcoder.domain.models.PromptModel;
import com.intellij.smartcoder.settings.BaseModelSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "DeepSeekSettings", storages = @Storage("deepseek_settings.xml"))
public class DeepSeekSettings implements BaseModelSettings, PersistentStateComponent<Element> {
    public static final String SETTINGS_TAG = "DeepSeekSettings";
    private static final String API_URL_TAG = "API_URL";
    private static final CredentialAttributes CREDENTIAL_ATTRIBUTES = new CredentialAttributes(DeepSeekSettings.class.getName(), "DS_BEARER_TOKEN");
    private static final String TAB_ACTION_TAG = "TAB_ACTION";
    private static final String TEMPERATURE_TAG = "TEMPERATURE";
    private static final String MAX_TOKENS_TAG = "MAX_TOKENS";
    private static final String TOP_P_TAG = "TOP_P";
    private static final String FREQUENCY_PENALTY_TAG = "FREQUENCY_PENALTY";
    private static final String PRESENCE_PENALTY_TAG = "PRESENCE_PENALTY";
    private static final String FIM_MODEL_TAG = "FIM_TOKEN_MODEL";
    private static final String MODEL = "MODEL";

    private String apiURL = "http://localhost:11434/api/generate";
    private TabActionOption tabActionOption = TabActionOption.ALL;
    private float temperature = 0.5f;
    private int maxTokens = 2048;
    private float topP = 0.9f;
    private float frequencyPenalty = 0f;
    private float presencePenalty = 0f;
    private PromptModel fimTokenModel = PromptModel.DEEPSEEK;
    private String model = "deepseek-coder:base";
    private boolean stream = false;

    private static final DeepSeekSettings deepSeekSettingsInstance = new DeepSeekSettings();

    @Override
    public @Nullable Element getState() {
        Element state = new Element(SETTINGS_TAG);
        state.setAttribute(API_URL_TAG, getApiURL());
        state.setAttribute(TAB_ACTION_TAG, getTabActionOption().name());
        state.setAttribute(TEMPERATURE_TAG, String.valueOf(getTemperature()));
        state.setAttribute(MAX_TOKENS_TAG, String.valueOf(getMaxTokens()));
        state.setAttribute(TOP_P_TAG, String.valueOf(getTopP()));
        state.setAttribute(FREQUENCY_PENALTY_TAG, String.valueOf(getFrequencyPenalty()));
        state.setAttribute(PRESENCE_PENALTY_TAG, String.valueOf(getPresencePenalty()));
        state.setAttribute(FIM_MODEL_TAG, getFimTokenModel().getId());
        state.setAttribute(MODEL, getModel());
        return state;
    }

    @Override
    public void loadState(@NotNull Element state) {
        if(state.getAttributeValue(API_URL_TAG)!=null){
            setApiURL(state.getAttributeValue(API_URL_TAG));
        }
        if(state.getAttributeValue(TAB_ACTION_TAG)!=null){
            setTabActionOption(TabActionOption.valueOf(state.getAttributeValue(TAB_ACTION_TAG)));
        }
        if(state.getAttributeValue(TEMPERATURE_TAG)!=null){
            setTemperature(state.getAttributeValue(TEMPERATURE_TAG));
        }
        if(state.getAttributeValue(MAX_TOKENS_TAG)!=null){
            setMaxTokens(state.getAttributeValue(MAX_TOKENS_TAG));
        }
        if(state.getAttributeValue(TOP_P_TAG)!=null){
            setTopP(state.getAttributeValue(TOP_P_TAG));
        }
        if(state.getAttributeValue(FREQUENCY_PENALTY_TAG)!=null){
            setFrequencyPenalty(state.getAttributeValue(FREQUENCY_PENALTY_TAG));
        }
        if(state.getAttributeValue(PRESENCE_PENALTY_TAG)!=null){
            setPresencePenalty(state.getAttributeValue(PRESENCE_PENALTY_TAG));
        }
        if(state.getAttributeValue(FIM_MODEL_TAG)!=null){
            setFimTokenModel(PromptModel.fromModel(state.getAttributeValue(FIM_MODEL_TAG)));
        }
        if(state.getAttributeValue(MODEL)!=null){
            setModel(state.getAttributeValue(MODEL));
        }
    }

    public static DeepSeekSettings getInstance() {
        if (ApplicationManager.getApplication() == null) {
            return deepSeekSettingsInstance;
        }

        DeepSeekSettings service = ApplicationManager.getApplication().getService(DeepSeekSettings.class);
        if(service == null) {
            return deepSeekSettingsInstance;
        }
        return service;
    }

    public void setApiToken(String apiToken) {
        PasswordSafe.getInstance().set(CREDENTIAL_ATTRIBUTES, new Credentials(null, apiToken));
    }

    @Override
    public String getApiToken() {
        Credentials credentials = PasswordSafe.getInstance().get(CREDENTIAL_ATTRIBUTES);
        return credentials != null ? credentials.getPasswordAsString() : "";
    }

    @Override
    public String getApiURL() {
        return apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    public TabActionOption getTabActionOption() {
        return tabActionOption;
    }

    public void setTabActionOption(TabActionOption tabActionOption) {
        this.tabActionOption = tabActionOption;
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = Float.parseFloat(temperature);
    }

    @Override
    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(String maxTokens) {
        this.maxTokens = Integer.parseInt(maxTokens);
    }

    @Override
    public float getTopP() {
        return topP;
    }

    public void setTopP(String topP) {
        this.topP = Float.parseFloat(topP);
    }

    @Override
    public float getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(String frequencyPenalty) {
        this.frequencyPenalty = Float.parseFloat(frequencyPenalty);
    }

    @Override
    public float getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(String presencePenalty) {
        this.presencePenalty = Float.parseFloat(presencePenalty);
    }

    @Override
    public PromptModel getFimTokenModel() {
        return fimTokenModel;
    }

    public void setFimTokenModel(PromptModel fimTokenModel) {
        this.fimTokenModel = fimTokenModel;
    }

    @Override
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
