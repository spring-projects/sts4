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
package org.springframework.ide.eclipse.boot.validation.preferences;

import java.util.List;

import org.springframework.ide.eclipse.boot.validation.BootValidationActivator;
import org.springframework.ide.eclipse.boot.validation.rules.ValidationRuleDefinitions;
import org.springframework.ide.eclipse.editor.support.preferences.AbstractProblemSeverityPreferencesPage;
import org.springframework.ide.eclipse.editor.support.preferences.EditorType;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

/**
 * @author Kris De Volder
 */
public class BootValidationProjectPropertyPage extends AbstractProblemSeverityPreferencesPage {

	public BootValidationProjectPropertyPage() {
		super(BootValidationPreferencesPage.util);
	}

	protected List<ProblemType> getProblemTypes() {
		return ValidationRuleDefinitions.getProblemTypes();
	}

	@Override
	protected String getEnableProjectPreferencesKey() {
		return BootValidationPreferencesPage.util.ENABLE_PROJECT_PREFERENCES(EditorType.JAVA);
	}

	@Override
	protected String getPluginId() {
		return BootValidationActivator.PLUGIN_ID;
	}

}
