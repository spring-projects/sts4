package org.springframework.ide.vscode.boot.metadata.types;

import org.springframework.ide.vscode.commons.util.text.IDocument;

@FunctionalInterface
public interface TypeUtilProvider {
	TypeUtil getTypeUtil(IDocument doc);
}
