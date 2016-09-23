package org.springframework.ide.vscode.commons.reconcile;

/**
 * Besides the methods below, the only hard requirement for a 'problem type' is
 * that it is a unique object that is not 'equals' to any other object.
 * <p>
 * It is probably nice if you implement a good toString however.
 * <p>
 * A good way to implement a discrete set of problemType objects is as an enum
 * that implements this interace.
 *
 * @author Kris De Volder
 */
public interface ProblemType {
	ProblemSeverity getDefaultSeverity();
	String toString();
}
