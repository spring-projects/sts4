/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Martin Lippert
 *
 */
public final class CommonsUIMessages extends NLS {

	private static final String BUNDLE_NAME =
			"org.springsource.ide.eclipse.commons.ui.CommonsUIMessages";

	private CommonsUIMessages() {
		// Do not instantiate
	}

	public static String ImageDescriptorRegistry_wrongDisplay;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CommonsUIMessages.class);
	}
}
