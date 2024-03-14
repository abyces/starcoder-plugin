package com.videogameaholic.intellij.starcoder;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class SmartCompletionService {
    public static PsiCodeBlock getEnclosingCodeBlock(Editor editor, PsiFile file) {
        // 获取光标在文件中的偏移量
        int offset = editor.getCaretModel().getOffset();
        // 在给定偏移量找到 PSI 元素
        PsiElement element = file.findElementAt(offset);
        // 使用 PsiTreeUtil 获取包含当前元素的最外层代码块
        PsiCodeBlock codeBlock = PsiTreeUtil.getParentOfType(element, PsiCodeBlock.class);
        return codeBlock;
    }

    public static boolean isCursorAtEndOfScope(Editor editor, PsiCodeBlock codeBlock) {
        if (codeBlock == null) {
            return false;
        }
        // 获取代码块的文本范围
        TextRange textRange = codeBlock.getTextRange();
        // 获取光标位置
        int cursorOffset = editor.getCaretModel().getOffset();
        // 检查光标是否在代码块的文本范围之后
        return cursorOffset >= textRange.getEndOffset();
    }
}
