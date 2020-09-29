/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Collection;

/**
 * A model that is capabable of deleting (at least some) of its elements should implement this.
 *
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public interface DeletionCapabableModel {

	void delete(Collection<BootDashElement> collection, UserInteractions ui);

	/**
	 * Assuming a given element belongs to this model, is this model capable of deleting the element?
	 */
	boolean canDelete(BootDashElement element);

	/**
	 * Create a message that will be shown to the user to ask them to confirm if they really want to go ahead and
	 * delete the given elements.
	 */
	String getDeletionConfirmationMessage(Collection<BootDashElement> value);
}
