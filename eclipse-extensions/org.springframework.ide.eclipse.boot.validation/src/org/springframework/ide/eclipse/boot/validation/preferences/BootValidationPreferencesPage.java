/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.preferences;

import java.util.List;

import org.springframework.ide.eclipse.boot.validation.BootValidationActivator;
import org.springframework.ide.eclipse.boot.validation.rules.BootValidationProblemType;
import org.springframework.ide.eclipse.editor.support.preferences.AbstractProblemSeverityPreferencesPage;
import org.springframework.ide.eclipse.editor.support.preferences.EditorType;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

/**
 * @author Kris De Volder
 */
public class BootValidationPreferencesPage extends AbstractProblemSeverityPreferencesPage {

	public static final ProblemSeverityPreferencesUtil util = new ProblemSeverityPreferencesUtil("boot.project.validation.builder.");

	public BootValidationPreferencesPage() {
		super(util);
	}


	protected List<ProblemType> getProblemTypes() {
		return BootValidationProblemType.values();
	}

	@Override
	protected String getEnableProjectPreferencesKey() {
		return util.ENABLE_PROJECT_PREFERENCES(EditorType.PROP);
	}

	@Override
	protected String getPluginId() {
		return BootValidationActivator.PLUGIN_ID;
	}

}
