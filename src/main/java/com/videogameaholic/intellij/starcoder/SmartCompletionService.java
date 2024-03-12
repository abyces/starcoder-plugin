package com.videogameaholic.intellij.starcoder;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class SmartCompletionService {
    public static PsiElement getScopeElementAtCaret(Editor editor) {
        PsiFile file = getCurrentPsiFile(editor.getProject());
        // 获取光标在文件中的偏移量
        int offset = editor.getCaretModel().getOffset();
        // 在给定偏移量找到 PSI 元素
        PsiElement element = file.findElementAt(offset);
        // 使用 PsiTreeUtil 获取作用域元素，例如方法或循环体
        PsiElement scopeElement = PsiTreeUtil.getParentOfType(element, PsiElement.class);
        return scopeElement;
    }

    public static PsiFile getCurrentPsiFile(Project project) {
        // 获取当前激活的编辑器
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        // 如果没有打开的编辑器，则返回 null
        if (editor == null) {
            return null;
        }
        // 使用编辑器实例来获取 PsiFile
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        return psiFile;
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
