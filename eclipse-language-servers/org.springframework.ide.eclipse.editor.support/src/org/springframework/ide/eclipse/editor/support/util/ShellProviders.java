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
package org.springframework.ide.eclipse.editor.support.util;

import javax.inject.Provider;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Convenience methods to addapt various types of objects representing  a 'ui context'
 * to a Shell{@link Provider}
 *
 * @author Kris De Volder
 */
public class ShellProviders {
	public static Provider<Shell> from(final ITextEditor editor) {
		return new Provider<Shell>() {
			@Override
			public Shell get() {
				return editor.getSite().getShell();
			}
		};
	}

	public static Provider<Shell> from(final Composite composite) {
		return new Provider<Shell>() {
			@Override
			public Shell get() {
				return composite.getShell();
			}
		};
	}
}
