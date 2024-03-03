package com.videogameaholic.intellij.starcoder.settings;

import com.intellij.application.options.editor.EditorOptionsProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.WindowManager;
import com.videogameaholic.intellij.starcoder.StarCoderWidget;
import com.videogameaholic.intellij.starcoder.settings.impl.DeepSeekSettings;
import groovy.util.logging.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class DeepSeekSettingsProvider implements EditorOptionsProvider {
    private SettingsPanel settingsPanel;

    @Override
    public @NotNull @NonNls String getId() {
        return "DeepSeek.Settings";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "DeepSeek";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if(settingsPanel == null) {
            settingsPanel = new SettingsPanel();
        }
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        DeepSeekSettings savedSettings = DeepSeekSettings.getInstance();

        return !savedSettings.getApiURL().equals(settingsPanel.getApiUrl())
                || !savedSettings.getApiToken().equals(settingsPanel.getApiToken())
                || savedSettings.getTabActionOption() != settingsPanel.getTabActionOption()
                || savedSettings.getTemperature() != Float.parseFloat(settingsPanel.getTemperature())
                || savedSettings.getMaxTokens() != Integer.parseInt(settingsPanel.getMaxNewTokens())
                || savedSettings.getTopP() != Float.parseFloat(settingsPanel.getTopP())
                || savedSettings.getFrequencyPenalty() != Float.parseFloat(settingsPanel.getRepetition())
                || savedSettings.getPresencePenalty() != Float.parseFloat(settingsPanel.getRepetition())
                || !savedSettings.getFimTokenModel().equals(settingsPanel.getFimTokenModel());
    }

    @Override
    public void apply() throws ConfigurationException {
        DeepSeekSettings savedSettings = DeepSeekSettings.getInstance();

        savedSettings.setApiURL(settingsPanel.getApiUrl());
        savedSettings.setApiToken(settingsPanel.getApiToken());
        savedSettings.setTabActionOption(settingsPanel.getTabActionOption());
        savedSettings.setTemperature(settingsPanel.getTemperature());
        savedSettings.setMaxTokens(settingsPanel.getMaxNewTokens());
        savedSettings.setTopP(settingsPanel.getTopP());
        savedSettings.setFrequencyPenalty(settingsPanel.getRepetition());
        savedSettings.setPresencePenalty(settingsPanel.getRepetition());
        savedSettings.setFimTokenModel(settingsPanel.getFimTokenModel());
        if(settingsPanel.getApiToken().isBlank()){
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            Project activeProject = null;
            for (Project project : projects) {
                Window window = WindowManager.getInstance().suggestParentWindow(project);
                if (window != null && window.isActive()) {
                    activeProject = project;
                }
            }
            Notifications.Bus.notify(
                    new Notification("DeepSeek","DeepSeek", "DeepSeek API token is recommended.", NotificationType.WARNING)
                    ,activeProject);
        }

        // Update the widget
        for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
            WindowManager.getInstance().getStatusBar(openProject).updateWidget(StarCoderWidget.ID);
        }
    }

    @Override
    public void reset() {
        DeepSeekSettings savedSettings = DeepSeekSettings.getInstance();

        settingsPanel.setApiUrl(savedSettings.getApiURL());
        settingsPanel.setApiToken(savedSettings.getApiToken());
        settingsPanel.setTabActionOption(savedSettings.getTabActionOption());
        settingsPanel.setTemperature(String.valueOf(savedSettings.getTemperature()));
        settingsPanel.setMaxNewTokens(String.valueOf(savedSettings.getMaxTokens()));
        settingsPanel.setTopP(String.valueOf(savedSettings.getTopP()));
        settingsPanel.setRepetition(String.valueOf(savedSettings.getFrequencyPenalty()));
        settingsPanel.setFimTokenModel(savedSettings.getFimTokenModel());
    }
}

