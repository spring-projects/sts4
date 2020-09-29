/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsources.ide.eclipse.commons.ui.linenumtoggle;

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.EditorsUI;

public class ToggleLineNumbersState extends org.eclipse.core.commands.State {

	private final IPreferenceStore store= EditorsUI.getPreferenceStore();
	private final String key = EDITOR_LINE_NUMBER_RULER;

	{
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(key)) {
					Object old = event.getOldValue();
					fireStateChanged(old);
				}
			}
		});
	}

	@Override
	public void setValue(Object _value) {
		store.setValue(key, (boolean)_value);
	}

	@Override
	public Object getValue() {
		return store.getBoolean(key);
	}
}