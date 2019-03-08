/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;

/**
 * Interface that represents the methods that one needs to implement in order
 * to define how  content assist proposal is applied to a IDocument
 *
 * @author Kris De Volder
 */
public interface ProposalApplier {

	/**
	 * {@link ProposalApplier} that does nothing whatsoever.
	 */
	static ProposalApplier NULL = new ProposalApplier() {
		@Override public IRegion getSelection() { return null; }
		@Override public void apply(IDocument doc) {}
		@Override public String toString() { return "NULL";};
	};

	IRegion getSelection() throws Exception;
	void apply(IDocument doc) throws Exception;

}
