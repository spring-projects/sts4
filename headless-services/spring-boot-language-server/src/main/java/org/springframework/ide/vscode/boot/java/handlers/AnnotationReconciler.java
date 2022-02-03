package org.springframework.ide.vscode.boot.java.handlers;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public interface AnnotationReconciler {
	
	void visit(IDocument doc, Annotation node, ITypeBinding typeBinding, IProblemCollector problemCollector);

}
