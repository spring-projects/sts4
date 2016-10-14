package org.springframework.ide.vscode.commons.reconcile;

/**
 * Minamal interface that objects representing a reconciler problem must
 * implement.
 *
 * @author Kris De Volder
 */
public interface ReconcileProblem {
	ProblemType getType();
	String getMessage();
	int getOffset();
	int getLength();
	String getCode();
}
