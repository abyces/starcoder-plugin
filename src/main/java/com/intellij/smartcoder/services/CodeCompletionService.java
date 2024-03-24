package com.intellij.smartcoder.services;

import com.intellij.openapi.editor.Editor;

import java.util.List;

public interface CodeCompletionService {
    String[] getCompletionHints(Editor focusedEditor, int cursorPosition);
    String[] getCodeCompletionHints(Editor focusedEditor, int cursorPosition);
    String replacementSuggestion (String prompt);
}
