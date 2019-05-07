/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * @author Alex Boyko
 *
 */
public class XmlConfigPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private List<Runnable> updateXmlSettingControlsEnablement = Collections.emptyList();
	private BooleanFieldEditor enableEditor;

	public XmlConfigPreferencePage() {
		super(GRID);
	}
	
	@Override
	protected void adjustGridLayout() {
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootLanguageServerPlugin.getDefault().getPreferenceStore());
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (enableEditor == event.getSource()) {
			updateXmlSettingControlsEnablement.forEach(r -> r.run());
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		updateXmlSettingControlsEnablement.forEach(r -> r.run());
	}

	@Override
	protected void createFieldEditors() {
		Composite fieldEditorParent = getFieldEditorParent();
		final boolean xmlEnabled = getPreferenceStore().getBoolean(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS);
		updateXmlSettingControlsEnablement = new ArrayList<>();
		
		enableEditor = new BooleanFieldEditor(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS, "Enable Spring XML Config files support", fieldEditorParent);
		addField(enableEditor);
		
		Label label = new Label(fieldEditorParent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.BEGINNING).create());
		
		Composite settingsComposite = new Composite(fieldEditorParent, SWT.NONE);
		settingsComposite.setLayoutData(GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.BEGINNING).create());
		settingsComposite.setLayout(new GridLayout(1, false));
		
		BooleanFieldEditor caFieldEditor = new BooleanFieldEditor(Constants.PREF_XML_CONFIGS_CONTENT_ASSIST, "Content Assist in editor", settingsComposite);
		addField(caFieldEditor);
		caFieldEditor.setEnabled(xmlEnabled, settingsComposite);
		updateXmlSettingControlsEnablement.add(() -> caFieldEditor.setEnabled(enableEditor.getBooleanValue(), settingsComposite));
		
		BooleanFieldEditor hyperlinkEditor = new BooleanFieldEditor(Constants.PREF_XML_CONFIGS_HYPERLINKS, "Hyperlinks in editor", settingsComposite);
		addField(hyperlinkEditor);
		hyperlinkEditor.setEnabled(xmlEnabled, settingsComposite);
		updateXmlSettingControlsEnablement.add(() -> hyperlinkEditor.setEnabled(enableEditor.getBooleanValue(), settingsComposite));
		
		Composite scanFoldersComposite = new Composite(settingsComposite, SWT.NONE);
		scanFoldersComposite.setFont(settingsComposite.getFont());
		scanFoldersComposite.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).align(GridData.FILL, GridData.BEGINNING).grab(true, false).create());
		scanFoldersComposite.setLayout(new GridLayout(2, false));
		StringFieldEditor scanGlobEditor = new StringFieldEditor(Constants.PREF_XML_CONFIGS_SCAN_FOLDERS, "Scan XML in folders for symbols:", scanFoldersComposite);
		addField(scanGlobEditor);
		scanGlobEditor.setEnabled(xmlEnabled, scanFoldersComposite);
		updateXmlSettingControlsEnablement.add(() -> scanGlobEditor.setEnabled(enableEditor.getBooleanValue(), scanFoldersComposite));
		
	}

}
