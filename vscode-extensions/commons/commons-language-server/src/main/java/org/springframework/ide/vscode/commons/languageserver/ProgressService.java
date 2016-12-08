package org.springframework.ide.vscode.commons.languageserver;

@FunctionalInterface
public interface ProgressService {

	/**
	 * Sends a progress event to the LSP client. A taskId is an arbirary id
	 * that can be chosen by the caller. The purpose of the id is to be a 'unique'
	 * id for some kind of 'long running job'. Only a single 'statusMsg' is associated
	 * with a given taskId at any one time. Each event updates the message shown
	 * to the user replacing the old one.
	 * <p>
	 * Updating the message to 'null' erases the previous message without showing
	 * a new one.
	 * <p>
	 * More than one message may be shown simultaneously to the user, if they
	 * have different taskId.
	 *
	 * @param taskId
	 * @param statusMsg
	 */
	void progressEvent(String taskId, String statusMsg);

}
