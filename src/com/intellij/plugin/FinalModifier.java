package com.intellij.plugin;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;

import java.util.List;

public class FinalModifier extends AnAction {
    public void actionPerformed(final AnActionEvent e) {
        final Editor editor = (Editor) e.getDataContext().getData("editor");
        if (editor == null) {
            return;
        }
        final Project project = (Project) e.getDataContext().getData("project");
        final PsiFile psiFile = (PsiFile) e.getDataContext().getData("psi.File");
        if (psiFile == null) {
            return;
        }
        final Document document = editor.getDocument();

        final List<HighlightInfo> list = DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.WARNING, project);

        for (final HighlightInfo highlighter : list) {
            if (String.valueOf(highlighter.getDescription()).contains("can have 'final' modifier")
                    || String.valueOf(highlighter.getDescription()).contains("may be 'final'")) {
                final int startOffset = highlighter.getStartOffset();
                final PsiIdentifier element = (PsiIdentifier) psiFile.findElementAt(startOffset);
                if (element != null) {
                    final PsiElement parent = element.getParent();
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            CommandProcessor.getInstance().executeCommand(project,
                                    new FinalModifier.InsertFinalModifier((PsiModifierListOwner) parent), "", null);
                        }

                    });
                }
            }
        }
    }

    private class InsertFinalModifier implements Runnable {
        private final PsiModifierListOwner modifierList;

        public InsertFinalModifier(final PsiModifierListOwner modifierList) {
            this.modifierList = modifierList;
        }

        public void run() {
            try {
                final PsiModifierList modList = this.modifierList.getModifierList();
                if (modList != null) {
                    modList.setModifierProperty("final", true);

                }
            } catch (final IncorrectOperationException e) {
                e.printStackTrace();
            }
        }
    }
}
