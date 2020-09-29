/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.springsource.ide.eclipse.commons.core.preferences.GlobalPreferenceEnhancer;

/**
 * This preferences page provides a shortcut for setting a group of preferences
 * that we believe make Eclipse more awesome
 *
 * @author Andrew Eisenberg
 * @since 3.3.0
 */
public class PreferencesCurator extends PreferencePage implements IWorkbenchPreferencePage {

	public final static String PREF_DIALOG_ID = "org.springsource.ide.eclipse.commons.curatorPreferencesPage";

	public PreferencesCurator() {
		super("Global Preferences Curator");
	}

	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
		setDescription("Sets/unsets a bunch of preferences that makes Eclipse more awesome");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout(1, true));

		Label desc = new Label(contents, SWT.WRAP);
		desc.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		desc.setText("We have scoured the Eclipse preferences pages and determined a handful of preferences\n"
				+ "that are not set in an optimal way.  Do you trust us?");

		Composite allComposite = new Composite(contents, SWT.BORDER);
		allComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		allComposite.setLayout(new GridLayout(1, false));
		Label label = new Label(allComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Set/Reset all curated preferences.");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		Button allSetButton = new Button(allComposite, SWT.PUSH);
		allSetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		allSetButton.setText("Set all preferences");
		allSetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().enhanceAllPreferences();
			}
		});
		Button allUnsetButton = new Button(allComposite, SWT.PUSH);
		allUnsetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		allUnsetButton.setText("Reset all preferences");
		allUnsetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().undoAll();
			}
		});

		Composite jdtComposite = new Composite(contents, SWT.BORDER);
		jdtComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		jdtComposite.setLayout(new GridLayout(1, false));
		Label jdtLabel = new Label(jdtComposite, SWT.NONE);
		jdtLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		jdtLabel.setText("JDT preferences");
		jdtLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		Label jdtLabelExplanation = new Label(jdtComposite, SWT.NONE);
		jdtLabelExplanation.setLayoutData(new GridData(SWT.BEGINNING, SWT.LEFT, false, false));
		jdtLabelExplanation.setText("We set the following JDT preferences:\n" + "* Editor smart paste (true)\n"
				+ "* Editor smart semicolon placement (true)\n" + "* Editor smart opening brace placement (true)\n"
				+ "* Editor smart backspace (true)\n" + "* Editor smart tab (true)\n"
				+ "* Escape text when pasting into a string literal (true)\n"
				+ "* Auto activation delay (40 ms)\n");
//				+ "* Auto activation triggers for Java (._abcdefghijklmnopqrstuvwxyz)\n");
		Button jdtSetButton = new Button(jdtComposite, SWT.PUSH);
		jdtSetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		jdtSetButton.setText("Set JDT preferences only");
		jdtSetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().enhanceJDTPreferences();
			}
		});
		Button jdtUnsetButton = new Button(jdtComposite, SWT.PUSH);
		jdtUnsetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		jdtUnsetButton.setText("Reset JDT preferences only");
		jdtUnsetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().undoJDTEnhance();
			}
		});
		PreferenceLinkArea jdtEditorArea = new PreferenceLinkArea(jdtComposite, SWT.WRAP,
				"org.eclipse.jdt.ui.preferences.SmartTypingPreferencePage",
				"Go to the <a>JDT smart typing preferences page</a>", (IWorkbenchPreferenceContainer) getContainer(),
				null);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		jdtEditorArea.getControl().setLayoutData(data);
		PreferenceLinkArea jdtContentAssistArea = new PreferenceLinkArea(jdtComposite, SWT.WRAP,
				"org.eclipse.jdt.ui.preferences.CodeAssistPreferencePage",
				"Go to the <a>JDT content assist preferences page</a>", (IWorkbenchPreferenceContainer) getContainer(),
				null);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		jdtContentAssistArea.getControl().setLayoutData(data);

		Composite m2eComposite = new Composite(contents, SWT.BORDER);
		m2eComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		m2eComposite.setLayout(new GridLayout(1, false));
		Label m2eLabel = new Label(m2eComposite, SWT.NONE);
		m2eLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		m2eLabel.setText("M2E preferences");
		m2eLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		Label m2eLabelExplanation = new Label(m2eComposite, SWT.NONE);
		m2eLabelExplanation.setLayoutData(new GridData(SWT.BEGINNING, SWT.LEFT, false, false));
		m2eLabelExplanation.setText("We set the following M2E preferences:\n"
				+ "* Hide folders of physically nested projects (true)\n"
				+ "* Download repository updates on startup (false)\n"
				+ "* Open XML page in pom editor by default (true)");
		Button m2eSetButton = new Button(m2eComposite, SWT.PUSH);
		m2eSetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		m2eSetButton.setText("Set M2E preferences only");
		m2eSetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().enhanceM2EPreferences();
			}
		});
		Button m2eUnsetButton = new Button(m2eComposite, SWT.PUSH);
		m2eUnsetButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		m2eUnsetButton.setText("Reset M2E preferences only");
		m2eUnsetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GlobalPreferenceEnhancer().undoJDTEnhance();
			}
		});
		PreferenceLinkArea m2eArea = new PreferenceLinkArea(m2eComposite, SWT.WRAP,
				"org.eclipse.m2e.core.preferences.Maven2PreferencePage", "Go to the <a>M2E preferences page</a>",
				(IWorkbenchPreferenceContainer) getContainer(), null);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		m2eArea.getControl().setLayoutData(data);
		PreferenceLinkArea m2eUIArea = new PreferenceLinkArea(m2eComposite, SWT.WRAP,
				"org.eclipse.m2e.core.ui.preferences.UserInterfacePreferencePage",
				"Go to the <a>M2E UI preferences page</a>", (IWorkbenchPreferenceContainer) getContainer(), null);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		m2eUIArea.getControl().setLayoutData(data);

		// disable m2e components if it is not installed
		if (Platform.getBundle("org.eclipse.m2e.core") == null) {
			m2eComposite.setEnabled(false);
			m2eLabel.setText(m2eLabel.getText() + " (not installed)");
			m2eSetButton.setEnabled(false);
			m2eUnsetButton.setEnabled(false);
			m2eUIArea.getControl().setEnabled(false);
			m2eArea.getControl().setEnabled(false);
		}

		return contents;
	}
}
