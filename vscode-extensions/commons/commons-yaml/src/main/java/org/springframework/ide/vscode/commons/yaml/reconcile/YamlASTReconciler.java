package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;

public interface YamlASTReconciler {
	void reconcile(YamlFileAST ast);
}
