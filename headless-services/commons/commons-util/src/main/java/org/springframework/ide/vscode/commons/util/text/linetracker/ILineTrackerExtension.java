/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util.text.linetracker;

/**
 * Extension interface for {@link org.eclipse.jface.text.ILineTracker}. Adds the
 * concept of rewrite sessions. A rewrite session is a sequence of replace
 * operations that form a semantic unit.
 *
 * @since 3.1
 */
public interface ILineTrackerExtension {

	/**
	 * Tells the line tracker that a rewrite session started. A rewrite session
	 * is a sequence of replace operations that form a semantic unit. The line
	 * tracker is allowed to use that information for internal optimization.
	 *
	 * @param session the rewrite session
	 * @throws IllegalStateException in case there is already an active rewrite
	 *             session
	 */
	void startRewriteSession(DocumentRewriteSession session) throws IllegalStateException;

	/**
	 * Tells the line tracker that the rewrite session has finished. This method
	 * is only called when <code>startRewriteSession</code> has been called
	 * before. The text resulting from the rewrite session is passed to the line
	 * tracker.
	 *
	 * @param session the rewrite session
	 * @param text the text with which to re-initialize the line tracker
	 */
	void stopRewriteSession(DocumentRewriteSession session, String text);
}
