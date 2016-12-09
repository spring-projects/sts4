package org.springframework.ide.vscode.application.properties.metadata.types;

import org.springframework.ide.vscode.commons.util.IDocument;

@FunctionalInterface
public interface TypeUtilProvider {
	TypeUtil getTypeUtil(IDocument doc);
}
