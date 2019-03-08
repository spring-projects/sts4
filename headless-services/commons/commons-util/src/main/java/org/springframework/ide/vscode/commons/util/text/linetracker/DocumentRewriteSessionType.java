/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * A document rewrite session type.
 * <p>
 * Allowed values are:
 * <ul>
 * 	<li>{@link DocumentRewriteSessionType#UNRESTRICTED}</li>
 * 	<li>{@link DocumentRewriteSessionType#UNRESTRICTED_SMALL} (since 3.3)</li>
 * 	<li>{@link DocumentRewriteSessionType#SEQUENTIAL}</li>
 * 	<li>{@link DocumentRewriteSessionType#STRICTLY_SEQUENTIAL}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.jface.text.IDocumentExtension4
 * @see org.eclipse.jface.text.IDocumentRewriteSessionListener
 * @since 3.1
 */
public class DocumentRewriteSessionType {

	/**
	 * An unrestricted rewrite session is a sequence of unrestricted replace operations. This
	 * session type should only be used for <em>large</em> operations that touch more than about
	 * fifty lines. Use {@link #UNRESTRICTED_SMALL} for small operations.
	 */
	public final static DocumentRewriteSessionType UNRESTRICTED= new DocumentRewriteSessionType();
	/**
	 * An small unrestricted rewrite session is a short sequence of unrestricted replace operations.
	 * This should be used for changes that touch less than about fifty lines.
	 *
	 * @since 3.3
	 */
	public final static DocumentRewriteSessionType UNRESTRICTED_SMALL= new DocumentRewriteSessionType();
	/**
	 * A sequential rewrite session is a sequence of non-overlapping replace
	 * operations starting at an arbitrary document offset.
	 */
	public final static DocumentRewriteSessionType SEQUENTIAL= new DocumentRewriteSessionType();
	/**
	 * A strictly sequential rewrite session is a sequence of non-overlapping
	 * replace operations from the start of the document to its end.
	 */
	public final static DocumentRewriteSessionType STRICTLY_SEQUENTIAL= new DocumentRewriteSessionType();


	/**
	 * Prohibit external object creation.
	 */
	private DocumentRewriteSessionType() {
	}
}
