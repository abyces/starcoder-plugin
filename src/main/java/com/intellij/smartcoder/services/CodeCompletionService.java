package com.intellij.smartcoder.services;

import com.intellij.openapi.editor.Editor;

import java.util.List;

public interface CodeCompletionService {
    String[] getCodeCompletionHints(Editor focusedEditor, CharSequence editorContents, int cursorPosition);
    String replacementSuggestion (String prompt);
}
