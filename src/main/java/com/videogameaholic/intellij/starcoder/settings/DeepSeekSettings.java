package com.videogameaholic.intellij.starcoder.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.videogameaholic.intellij.starcoder.PromptModel;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@State(name = "DeepSeekSettings", storages = @Storage("deepseek_settings.xml"))
public class DeepSeekSettings implements PersistentStateComponent<Element> {
    public static final String SETTINGS_TAG = "DeepSeekSettings";
    private static final String API_URL_TAG = "API_URL";
    private static final CredentialAttributes CREDENTIAL_ATTRIBUTES = new CredentialAttributes(DeepSeekSettings.class.getName(), "DS_BEARER_TOKEN");
    private static final String TAB_ACTION_TAG = "TAB_ACTION";
    private static final String TEMPERATURE_TAG = "TEMPERATURE";
    private static final String MAX_NEW_TOKENS_TAG = "MAX_NEW_TOKENS";
    private static final String TOP_P_TAG = "TOP_P";
    private static final String REPEAT_PENALTY_TAG = "REPEAT_PENALTY";
    private static final String FIM_MODEL_TAG = "FIM_TOKEN_MODEL";
    private static final String MODEL = "MODEL";
    private static final String MESSAGE = "DIALOGUE";

    private boolean saytEnabled = true;
    private String apiURL = "https://api.deepseek.com/v1/chat/completions";
    private TabActionOption tabActionOption = TabActionOption.ALL;
    private float temperature = 0.2f;
    private int maxNewTokens = 256;
    private float topP = 0.9f;
    private float repetitionPenalty = 1.2f;
    private PromptModel fimTokenModel = PromptModel.DEEPSEEK;
    private String model = "deepseek-coder";
    private String message = "[\n{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n{\"role\": \"user\", \"content\": \"{}\"}]";


    private static final DeepSeekSettings deepSeekSettingsInstance = new DeepSeekSettings();

    @Override
    public @Nullable Element getState() {
        Element state = new Element(SETTINGS_TAG);
        state.setAttribute(API_URL_TAG, getApiURL());
        state.setAttribute(TAB_ACTION_TAG, getTabActionOption().name());
        state.setAttribute(TEMPERATURE_TAG, String.valueOf(getTemperature()));
        state.setAttribute(MAX_NEW_TOKENS_TAG, String.valueOf(getMaxNewTokens()));
        state.setAttribute(TOP_P_TAG, String.valueOf(getTopP()));
        state.setAttribute(REPEAT_PENALTY_TAG, String.valueOf(getRepetitionPenalty()));
        state.setAttribute(FIM_MODEL_TAG, getFimTokenModel().getId());
        state.setAttribute(MODEL, getModel());
        state.setAttribute(MESSAGE, getMessage());
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
        if(state.getAttributeValue(MAX_NEW_TOKENS_TAG)!=null){
            setMaxNewTokens(state.getAttributeValue(MAX_NEW_TOKENS_TAG));
        }
        if(state.getAttributeValue(TOP_P_TAG)!=null){
            setTopP(state.getAttributeValue(TOP_P_TAG));
        }
        if(state.getAttributeValue(REPEAT_PENALTY_TAG)!=null){
            setRepetitionPenalty(state.getAttributeValue(REPEAT_PENALTY_TAG));
        }
        if(state.getAttributeValue(FIM_MODEL_TAG)!=null){
            setFimTokenModel(PromptModel.fromId(state.getAttributeValue(FIM_MODEL_TAG)));
        }
        if(state.getAttributeValue(MODEL)!=null){
            setModel(state.getAttributeValue(MODEL));
        }
        if(state.getAttributeValue(MESSAGE)!=null){
            setMessage(state.getAttributeValue(MESSAGE));
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

    public boolean isSaytEnabled() {
        return saytEnabled;
    }

    public void setSaytEnabled(boolean saytEnabled) {
        this.saytEnabled = saytEnabled;
    }

    public void toggleSaytEnabled() {
        this.saytEnabled = !this.saytEnabled;
    }

    public String getApiURL() {
        return apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    public String getApiToken() {
        Credentials credentials = PasswordSafe.getInstance().get(CREDENTIAL_ATTRIBUTES);
        return credentials != null ? credentials.getPasswordAsString() : "";
    }

    public void setApiToken(String apiToken) {
        PasswordSafe.getInstance().set(CREDENTIAL_ATTRIBUTES, new Credentials(null, apiToken));
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = Float.parseFloat(temperature);
    }

    public int getMaxNewTokens() {
        return maxNewTokens;
    }

    public void setMaxNewTokens(String maxNewTokens) {
        this.maxNewTokens = Integer.parseInt(maxNewTokens);
    }

    public float getTopP() {
        return topP;
    }

    public void setTopP(String topP) {
        this.topP = Float.parseFloat(topP);
    }

    public float getRepetitionPenalty() {
        return repetitionPenalty;
    }

    public void setRepetitionPenalty(String repetitionPenalty) {
        this.repetitionPenalty = Float.parseFloat(repetitionPenalty);
    }

    public TabActionOption getTabActionOption() {
        return tabActionOption;
    }

    public void setTabActionOption(TabActionOption tabActionOption) {
        this.tabActionOption = tabActionOption;
    }

    public PromptModel getFimTokenModel(){
        return fimTokenModel;
    }

    public void setFimTokenModel(PromptModel fimTokenModel){
        this.fimTokenModel=fimTokenModel;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

