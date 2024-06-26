package com.intellij.smartcoder;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.smartcoder.services.CodeCompletionService;
import com.intellij.util.Consumer;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import com.intellij.smartcoder.domain.enums.SmartCoderStatus;
import com.intellij.smartcoder.services.impl.CodeCompletionServiceImpl;
import groovy.util.logging.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SmartCoderWidget extends EditorBasedWidget
implements StatusBarWidget.Multiframe, StatusBarWidget.IconPresentation,
        CaretListener, SelectionListener, BulkAwareDocumentListener.Simple, PropertyChangeListener, Disposable {
    public static final String ID = "SmartCoderWidget";

    public static final Key<String[]> STAR_CODER_CODE_SUGGESTION = new Key<>("SmartCoder Code Suggestion");
    public static final Key<Integer> STAR_CODER_POSITION = new Key<>("SmartCoder Position");
    private static final String SWING_FOCUS_OWNER_PROPERTY = "SwingFocusOwner";

    private MergingUpdateQueue serviceQueue;

    private boolean startCodeCompletion = false;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> scheduledFuture;

    protected SmartCoderWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public StatusBarWidget copy() {
        return new SmartCoderWidget(getProject());
    }

    @Override
    public void dispose() {}

    @Override
    public @Nullable Icon getIcon() {
        CodeCompletionServiceImpl codeCompletionService = ApplicationManager.getApplication().getService(CodeCompletionServiceImpl.class);
        SmartCoderStatus status = SmartCoderStatus.getStatusByCode(codeCompletionService.getStatusCode());
        if(status == SmartCoderStatus.OK) {
            return SmartCoderIcons.WidgetEnabled;
        } else {
            return SmartCoderIcons.WidgetError;
        }
    }

    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        StringBuilder toolTipText = new StringBuilder("SmartCoder");
        toolTipText.append("enabled");
        CodeCompletionServiceImpl codeCompletionService = ApplicationManager.getApplication().getService(CodeCompletionServiceImpl.class);
        int statusCode = codeCompletionService.getStatusCode();
        SmartCoderStatus status = SmartCoderStatus.getStatusByCode(statusCode);
        switch (status) {
            case OK:
                break;
            case UNKNOWN:
                toolTipText.append(" (http error ");
                toolTipText.append(statusCode);
                toolTipText.append(")");
                break;
            default:
                toolTipText.append(" (");
                toolTipText.append(status.getDisplayValue());
                toolTipText.append(")");
        }

        return toolTipText.toString();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        // Toggle if the plugin is enabled.
        return mouseEvent -> {
            if(myStatusBar != null) {
                myStatusBar.updateWidget(ID);
            }
        };
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        serviceQueue = new MergingUpdateQueue("SmartCoderServiceQueue",1000,true,
                null,this, null, false);
        serviceQueue.setRestartTimerOnAdd(true);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addCaretListener(this, this);
        multicaster.addSelectionListener(this, this);
        multicaster.addDocumentListener(this,this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY, this);
        Disposer.register(this,
                () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(SWING_FOCUS_OWNER_PROPERTY,
                        this)
        );
    }

    private Editor getFocusOwnerEditor() {
        Component component = getFocusOwnerComponent();
        Editor editor = component instanceof EditorComponentImpl ? ((EditorComponentImpl)component).getEditor() : getEditor();
        return editor != null && !editor.isDisposed() ? editor : null;
    }

    private Component getFocusOwnerComponent() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) {
            IdeFocusManager focusManager = IdeFocusManager.getInstance(getProject());
            Window frame = focusManager.getLastFocusedIdeWindow();
            if (frame != null) {
                focusOwner = focusManager.getLastFocusedFor(frame);
            }
        }
        return focusOwner;
    }

    private boolean isFocusedEditor(Editor editor) {
        Component focusOwner = getFocusOwnerComponent();
        return focusOwner == editor.getContentComponent();
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent e) {
        startCodeCompletion = true;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateInlayHints(getFocusOwnerEditor());
    }

    @Override
    public void selectionChanged(SelectionEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretAdded(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretRemoved(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void afterDocumentChange (@NotNull Document document) {
        if(ApplicationManager.getApplication().isDispatchThread()) {
            if (scheduledFuture != null && !scheduledFuture.isDone()) {
                scheduledFuture.cancel(true);
            }

            scheduledFuture = executor.schedule(() -> {
                EditorFactory.getInstance().editors(document)
                        .filter(this::isFocusedEditor)
                        .findFirst()
                        .ifPresent(this::updateInlayHints);
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    private void updateInlayHints(Editor focusedEditor) {
        if(focusedEditor == null) return;
        // TODO File extension exclusion settings?
        VirtualFile file = FileDocumentManager.getInstance().getFile(focusedEditor.getDocument());
        if (file == null) return;

        // If a selection is highlighted, clear all hints.
        String selection = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        if(selection != null && selection.length() > 0) {
            String[] existingHints = file.getUserData(STAR_CODER_CODE_SUGGESTION);
            if (existingHints != null && existingHints.length > 0) {
                file.putUserData(STAR_CODER_CODE_SUGGESTION, null);
                file.putUserData(STAR_CODER_POSITION, focusedEditor.getCaretModel().getOffset());

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            }
            return;
        }

        Integer starCoderPos = file.getUserData(STAR_CODER_POSITION);
        int lastPosition = (starCoderPos==null) ? 0 : starCoderPos;
        int currentPosition = focusedEditor.getCaretModel().getOffset();

        // If cursor hasn't moved, don't do anything.
        if (lastPosition == currentPosition) return;

        // Check the existing inline hint (not blocks) if it exists.
        InlayModel inlayModel = focusedEditor.getInlayModel();
        if (currentPosition > lastPosition) {
            String[] existingHints = file.getUserData(STAR_CODER_CODE_SUGGESTION);
            if (existingHints != null && existingHints.length > 0) {
                String inlineHint = existingHints[0];
                String modifiedText = focusedEditor.getDocument().getCharsSequence().subSequence(lastPosition, currentPosition).toString();
                if(modifiedText.startsWith("\n")) {
                    // If the user typed Enter, the editor may have auto-spaced for alignment.
                    modifiedText = modifiedText.replace(" ","");
                    // TODO Count the spaces and remove from the next block hint, or just remove
                    // leading spaces from the block hint before moving up?
                    // example: set a boolean here and do existingHints[1] = existingHints[1].stripLeading()
                    // The problem is that the spaces are split in the update, some spaces are included after the carriage return,
                    // (in the caret position update) but then after document change has more spaces in it.
                }
                // See if they typed the same thing that we suggested.
                if (inlineHint.startsWith(modifiedText)) {
                    // Update the hint rather than calling the API to suggest a new one.
                    inlineHint = inlineHint.substring(modifiedText.length());
                    if(inlineHint.length()>0) {
                        // We only need to modify the inline hint and any block hints will remain unchanged.
                        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
                        inlayModel.addInlineElement(currentPosition, true, new CodeGenHintRenderer(inlineHint));
                        existingHints[0] = inlineHint;

                        // Update the UserData
                        file.putUserData(STAR_CODER_CODE_SUGGESTION, existingHints);
                        file.putUserData(STAR_CODER_POSITION, currentPosition);
                        return;
                    } else if (existingHints.length > 1) {
                        // If the first line has been completely inserted, and there are more lines, move them up.
                        existingHints = Arrays.copyOfRange(existingHints, 1, existingHints.length);
                        addCodeSuggestion(focusedEditor, file, currentPosition, existingHints);
                        return;
                    } else {
                        // We ran out of inline hint and there are no block hints,
                        // So clear the hints now, and we'll call the API below.
                        file.putUserData(STAR_CODER_CODE_SUGGESTION, null);
                    }
                }
            }
        }

        // If we made it through all that, clear all hints and call the API.
        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
        inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);

        // Update position immediately to prevent repeated calls.
        file.putUserData(STAR_CODER_POSITION, currentPosition);

        CodeCompletionService codeCompletionService = ApplicationManager.getApplication().getService(CodeCompletionServiceImpl.class);

        if (!startCodeCompletion)
            return;

        serviceQueue.queue(Update.create(focusedEditor,() -> {
            String[] hintList = codeCompletionService.getCompletionHints(focusedEditor, currentPosition);
            this.addCodeSuggestion(focusedEditor, file, currentPosition, hintList);
        }));
    }

    private void addCodeSuggestion(Editor focusedEditor, VirtualFile file, int suggestionPosition, String[] hintList) {
        WriteCommandAction.runWriteCommandAction(focusedEditor.getProject(), () -> {
            // Discard this update if the position has changed or text is now selected.
            if (suggestionPosition != focusedEditor.getCaretModel().getOffset()) {
                System.out.println("StarCoderWidget.addCodeSuggestion -> Position changed. from: " + suggestionPosition + " to: " + focusedEditor.getCaretModel().getOffset());
                if (suggestionPosition != focusedEditor.getCaretModel().getOffset() - 4) {return;}
            }
            if (focusedEditor.getSelectionModel().getSelectedText() != null) {
                System.out.println("StarCoderWidget.addCodeSuggestion -> Text selected.");
                return;
            }
            file.putUserData(STAR_CODER_CODE_SUGGESTION, hintList);
            file.putUserData(STAR_CODER_POSITION, focusedEditor.getCaretModel().getOffset());
            InlayModel inlayModel = focusedEditor.getInlayModel();
            inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            if (hintList != null && hintList.length > 0) {
                // The first line is an inline element
                if (hintList[0].trim().length() > 0) {
                    inlayModel.addInlineElement(focusedEditor.getCaretModel().getOffset(), true, new CodeGenHintRenderer(hintList[0]));
                }
                // Each additional line is a block element
                for (int i = 1; i < hintList.length; i++) {
                    inlayModel.addBlockElement(focusedEditor.getCaretModel().getOffset(), false, false, 0, new CodeGenHintRenderer(hintList[i]));
                }
            }
        });
    }
}
