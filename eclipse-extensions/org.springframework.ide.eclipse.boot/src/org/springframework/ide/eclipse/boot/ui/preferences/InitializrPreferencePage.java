/*******************************************************************************
 *  Copyright (c) 2016 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_INITIALIZR_URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;

/**
 * Preferences page for Spring IO Initializr IDE support
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class InitializrPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String LABEL_INITIALIZR_URL = "Initializr URLs";
	private static final String TOOLTIP_INITIALIZR_URL = "Spring Initializr server URL";
	private static final String MSG_INVALID_URL_FORMAT = "Invalid URL format";

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		ListEditor initializrUrl = new ListEditor(PREF_INITIALIZR_URL, LABEL_INITIALIZR_URL, parent) {


			@Override
			protected String createList(String[] items) {
				return BootPreferences.encodeUrls(items);
			}

			@Override
			protected String getNewInputObject() {
				return readUrl();
			}

			private String readUrl() {
				InputDialog id = new InputDialog(getShell(), "Add Url", "Enter Url",
						"https://", new IInputValidator() {
					@Override
					public String isValid(String newText) {
						try {
							new URL(newText);
						} catch (MalformedURLException e) {
							return MSG_INVALID_URL_FORMAT;
						}
						return null;
					}
				});
				if (id.open() == Window.OK) {
					return id.getValue();
				} else {
					return null;
				}
			}

			@Override
			protected String[] parseString(String stringList) {
				return BootPreferences.decodeUrl(stringList);
			}

		};
		initializrUrl.getLabelControl(parent).setToolTipText(TOOLTIP_INITIALIZR_URL);
		initializrUrl.getListControl(parent).setToolTipText(TOOLTIP_INITIALIZR_URL);
		addField(initializrUrl);

	}

}
