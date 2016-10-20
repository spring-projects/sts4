package org.springframework.ide.vscode.commons.yaml.reconcile;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public final class YamlSchemaBasedReconcileEngine extends YamlReconcileEngine {
	private final YamlSchema schema;

	public YamlSchemaBasedReconcileEngine(YamlASTProvider parser, YamlSchema schema) {
		super(parser);
		this.schema = schema;
	}

	@Override
	protected ReconcileProblem syntaxError(String msg, int offset, int length) {
		return YamlSchemaProblems.syntaxProblem(msg, offset, length);
	}

	@Override
	protected YamlASTReconciler getASTReconciler(IDocument doc, IProblemCollector problems) {
		return new SchemaBasedYamlASTReconciler(problems, schema);
	}
}