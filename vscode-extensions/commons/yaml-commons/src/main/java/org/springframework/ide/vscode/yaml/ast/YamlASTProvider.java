package org.springframework.ide.vscode.yaml.ast;

import org.springframework.ide.vscode.commons.reconcile.IDocument;

@FunctionalInterface
public interface YamlASTProvider {
	YamlFileAST getAST(IDocument doc) throws Exception;
}
