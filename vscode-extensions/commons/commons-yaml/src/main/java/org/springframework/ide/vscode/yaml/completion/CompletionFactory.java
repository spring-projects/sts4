package org.springframework.ide.vscode.yaml.completion;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.yaml.schema.YType;
import org.springframework.ide.vscode.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.yaml.schema.YTypedProperty;

public interface CompletionFactory {

	CompletionFactory DEFAULT = new DefaultCompletionFactory();

	ICompletionProposal beanProperty(IDocument doc, String contextProperty, YType contextType, String query, YTypedProperty p, double score, DocumentEdits edits, YTypeUtil typeUtil);
	ICompletionProposal valueProposal(String value, String query, String label, YType type, double score, DocumentEdits edits);

}
