package com.intellij.smartcoder.services.impl;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.smartcoder.domain.enums.CompletionType;
import com.intellij.smartcoder.services.CodeAnalyseService;

import java.util.Objects;

public class CodeAnalyseServiceImpl implements CodeAnalyseService {
    /**
     *
     * @param focusedEditor
     * @param editorContents
     * @param cursorPosition
     * @return
     */
    @Override
    public CompletionType preProcessEditorContent(Editor focusedEditor, CharSequence editorContents, int cursorPosition) {
        PsiFile psiFile = PsiDocumentManager.getInstance(Objects.requireNonNull(focusedEditor.getProject())).getPsiFile(focusedEditor.getDocument());
        if (psiFile != null) {
            PsiCodeBlock codeBlock = getEnclosingCodeBlock(focusedEditor, psiFile);
            if (codeBlock != null) {
                boolean isCursorAtEnd = isCursorAtEndOfScope(codeBlock, cursorPosition);
                return isCursorAtEnd ? CompletionType.ONE_LINE : CompletionType.MULTI_LINE;
            }
        }
        return CompletionType.MULTI_LINE;
    }

    /**
     *
     * @param focusedEditor
     * @param editorContents
     * @param responseCode
     * @return
     */
    @Override
    public String postProcessResponseCode(Editor focusedEditor, CharSequence editorContents, String responseCode) {
        return null;
    }

    private PsiCodeBlock getEnclosingCodeBlock(Editor editor, PsiFile file) {
        // 获取光标在文件中的偏移量
        int offset = editor.getCaretModel().getOffset();
        // 在给定偏移量找到 PSI 元素
        PsiElement element = file.findElementAt(offset);
        // 使用 PsiTreeUtil 获取包含当前元素的最外层代码块
        PsiCodeBlock codeBlock = PsiTreeUtil.getParentOfType(element, PsiCodeBlock.class);
        return codeBlock;
    }

    private boolean isCursorAtEndOfScope(PsiCodeBlock codeBlock, int cursorPosition) {
        if (codeBlock == null) {
            return false;
        }
        TextRange textRange = codeBlock.getTextRange();
        return cursorPosition >= textRange.getEndOffset();
    }
}
