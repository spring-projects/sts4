/*******************************************************************************
 *  Copyright (c) 2015, 2017 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import static org.springframework.ide.eclipse.boot.core.BootPreferences.*;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_BOOT_FAST_STARTUP_JVM_ARGS;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_BOOT_FAST_STARTUP_REMIND_MESSAGE;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_BOOT_PROJECT_EXCLUDE;
import static org.springframework.ide.eclipse.boot.core.BootPreferences.PREF_IGNORE_SILENT_EXIT;

import org.eclipse.debug.internal.ui.preferences.BooleanFieldEditor2;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.springframework.ide.eclipse.boot.core.BootActivator;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class BootPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public BootPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BootActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		Group generalGroup = new Group(parent, SWT.NONE);
		generalGroup.setText("General");
		generalGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		generalGroup.setLayout(GridLayoutFactory.fillDefaults().create());

		StringFieldEditor projectExclude = new RegExpFieldEditor(PREF_BOOT_PROJECT_EXCLUDE, "Exclude Projects", generalGroup);
		setTooltip(generalGroup, projectExclude, "Any project who's name matches this regexp will NOT be treated as a Spring Boot App");
		addField(projectExclude);

		BooleanFieldEditor2 ignoreSilentExit = new BooleanFieldEditor2(PREF_IGNORE_SILENT_EXIT, "Ignore Silent Exit", SWT.CHECK, generalGroup);
		setTooltip(generalGroup, ignoreSilentExit, "When debugging a Boot App, do not suspend when 'SilentExitException' is raised. "
				+ "(This exception is raised by spring-boot-devtools as part of its normal operation)");
		addField(ignoreSilentExit);

		Group launchGroup = new Group(parent, SWT.NONE);
		launchGroup.setText("Fast Startup");
		launchGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		launchGroup.setLayout(GridLayoutFactory.fillDefaults().create());

		addField(new StringFieldEditor(PREF_BOOT_FAST_STARTUP_JVM_ARGS, "Java VM arguments to trigger app's fast startup", launchGroup));

		addField(new BooleanFieldEditor2(PREF_BOOT_FAST_STARTUP_DEFAULT, "Default for new Boot launch configurations", SWT.CHECK, launchGroup));

		addField(new BooleanFieldEditor2(PREF_BOOT_FAST_STARTUP_REMIND_MESSAGE, "Show warning message when Fast Startup is turned on", SWT.CHECK, launchGroup));

		Composite thinLauncherComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(thinLauncherComposite);
		FileFieldEditor thinLauncher = new FileFieldEditor(PREF_BOOT_THIN_WRAPPER, "Thin Wrapper", true, StringFieldEditor.VALIDATE_ON_KEY_STROKE, thinLauncherComposite);
		thinLauncher.setFileExtensions(new String[] { "*.jar" });
		thinLauncher.setErrorMessage("Thin launcher must be an existing file");
		setTooltip(thinLauncherComposite, thinLauncher, "Thin boot launcher jar to use in Spring Boot Launch configuration (when that option is enabled in the launch config)");
		addField(thinLauncher);
	}



	@Override
	protected void adjustGridLayout() {
		// Do nothing. Page offers one column grid layout. Group controls layout fields appropriately.
	}

	private void setTooltip(Composite parent, StringFieldEditor fe, String tooltip) {
		fe.getLabelControl(parent).setToolTipText(tooltip);
		fe.getTextControl(parent).setToolTipText(tooltip);
	}

	private void setTooltip(Composite parent, BooleanFieldEditor2 fe, String tooltip) {
		fe.getChangeControl(parent).setToolTipText(tooltip);
	}

}
