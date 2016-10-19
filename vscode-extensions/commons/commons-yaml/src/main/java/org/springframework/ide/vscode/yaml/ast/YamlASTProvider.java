package org.springframework.ide.vscode.yaml.ast;

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

@FunctionalInterface
public interface YamlASTProvider {
	YamlFileAST getAST(IDocument doc) throws Exception;
}
