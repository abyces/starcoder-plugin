package com.intellij.smartcoder.services;

import com.intellij.openapi.editor.Editor;
import com.intellij.smartcoder.domain.enums.CompletionType;

public interface CodeAnalyseService {
    CompletionType preProcessEditorContent(Editor focusedEditor, CharSequence editorContents, int cursorPostion);
    String postProcessResponseCode(Editor focusedEditor, CharSequence editorContents, String responseCode);
}
