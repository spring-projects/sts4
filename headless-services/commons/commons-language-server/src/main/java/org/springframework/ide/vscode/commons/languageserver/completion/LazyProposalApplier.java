/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.completion;

import java.util.concurrent.Callable;

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
