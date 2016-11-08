package org.springframework.ide.vscode.application.yaml.completions;

import java.util.concurrent.Callable;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Temprary placeholder which sort of replaces the LazyProposalApplier from old STS.
 * It really does nothing right now. Somehow this should be tied into LS protocol so
 * that the edits are not computed until a completion is being resolved.
 * <p>
 * Right now this is not lazy at all and the completion edits are just computed immediatly.
 */
public class LazyProposalApplier {
	
	public static DocumentEdits from(Callable<DocumentEdits> createEdits) throws Exception {
		return createEdits.call();
	}

}
