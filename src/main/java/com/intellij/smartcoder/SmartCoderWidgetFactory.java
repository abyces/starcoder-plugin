package com.intellij.smartcoder;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class SmartCoderWidgetFactory extends StatusBarEditorBasedWidgetFactory implements Disposable {
    @Override
    public @NonNls @NotNull String getId() {
        return SmartCoderWidget.ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "SmartCoder";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new SmartCoderWidget(project);
    }

    @Override
    public void dispose() {}
}
