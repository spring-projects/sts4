/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.console;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.IOConsole;

@SuppressWarnings("restriction")
public class LanguageServerIOConsole extends IOConsole implements IPropertyChangeListener {

	private static final String CONSOLE_TYPE = LanguageServerIOConsole.class.getName();

	public LanguageServerIOConsole(final String title) {
		super(title, CONSOLE_TYPE, null);
	}

	@Override
	protected void init() {
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
		if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT)) {
			int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
			int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
			setWaterMarks(lowWater, highWater);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getProperty();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		if (property.equals(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK)) {
			boolean limitBufferSize = store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT);
			if (limitBufferSize) {
				int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
				int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
				if (highWater > lowWater) {
					setWaterMarks(lowWater, highWater);
				}
			} else {
				setWaterMarks(-1, -1);
			}
		}
	}

	@Override
	protected void dispose() {
		super.dispose();
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

}