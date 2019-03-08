/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

/**
 * YAML editor based on Generic editor.
 * Has spaces rather than tab for indentation
 *
 * @author Alex Boyko
 */
@SuppressWarnings("restriction")
public class YamlGenericEditor extends ExtensionBasedTextEditor {
	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return true;
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();

		List<IPreferenceStore> stores = new ArrayList<>(3);
		stores.add(LanguageServerCommonsActivator.getInstance().getPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());
		stores.add(PlatformUI.getPreferenceStore());

		setPreferenceStore(new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()])));

	}

}
