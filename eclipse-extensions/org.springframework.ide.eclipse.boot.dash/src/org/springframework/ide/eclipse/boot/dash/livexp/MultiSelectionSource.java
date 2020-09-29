/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;

/**
 * Page sections can be a source of selections (meaning user can select elements in them).
 * If so, they should implement this interface. This will allow pages or other sections to obtain
 * their selection and listen to changes on the selection.
 *
 * @author Kris De Volder
 */
public interface MultiSelectionSource extends IPageSection {
	MultiSelection<?> getSelection();
}
