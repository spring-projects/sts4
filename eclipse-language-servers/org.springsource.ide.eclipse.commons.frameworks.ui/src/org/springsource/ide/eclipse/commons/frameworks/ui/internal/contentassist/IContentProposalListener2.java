/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist;


/**
 * This interface is used to listen to additional notifications from a
 * {@link ContentProposalAdapter}.
 * @author Kris De Volder
 * @author Nieraj Singh
 * @since 3.3
 */
public interface IContentProposalListener2 {
	/**
	 * A content proposal popup has been opened for content proposal assistance.
	 * 
	 * @param adapter
	 *           the ContentProposalAdapter which is providing content proposal
	 *           behavior to a control
	 */
	public void proposalPopupOpened(ContentProposalAdapter adapter);

	/**
	 * A content proposal popup has been closed.
	 * 
	 * @param adapter
	 *           the ContentProposalAdapter which is providing content proposal
	 *           behavior to a control
	 */
	public void proposalPopupClosed(ContentProposalAdapter adapter);
}
