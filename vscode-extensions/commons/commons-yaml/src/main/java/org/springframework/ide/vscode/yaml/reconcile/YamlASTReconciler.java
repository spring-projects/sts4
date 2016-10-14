package org.springframework.ide.vscode.yaml.reconcile;

import org.springframework.ide.vscode.yaml.ast.YamlFileAST;

public interface YamlASTReconciler {
	void reconcile(YamlFileAST ast);
}
