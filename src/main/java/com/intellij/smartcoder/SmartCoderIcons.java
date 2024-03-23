package com.intellij.smartcoder;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface SmartCoderIcons {
    Icon Action = IconLoader.getIcon("/icons/actionIcon.svg", SmartCoderIcons.class);
    Icon WidgetEnabled = IconLoader.getIcon("/icons/widgetEnabled.svg", SmartCoderIcons.class);
    Icon WidgetDisabled = IconLoader.getIcon("/icons/widgetDisabled.svg", SmartCoderIcons.class);
    Icon WidgetError = IconLoader.getIcon("/icons/widgetError.svg", SmartCoderIcons.class);
}
