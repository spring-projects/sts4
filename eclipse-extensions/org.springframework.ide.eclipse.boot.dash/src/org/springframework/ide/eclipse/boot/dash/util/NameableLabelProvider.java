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
package org.springframework.ide.eclipse.boot.dash.util;

import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;

/**
 * Label provider that simly displays the 'name' of any elements that
 * implement {@link Nameable}.
 *
 * @author Kris De Volder
 */
public final class NameableLabelProvider extends SimpleLabelProvider {
	public String getText(Object element) {
		if (element instanceof Nameable) {
			return ((Nameable) element).getName();
		}
		return super.getText(element);
	}
}