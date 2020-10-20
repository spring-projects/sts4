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
package org.springframework.ide.eclipse.editor.support.preferences;

import java.io.IOException;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * @author Kris De Volder
 */
public class ProblemSeverityPreferencesUtil {

	public final String PREFERENCE_PREFIX;// = "spring.properties.editor.problem.";

	public ProblemSeverityPreferencesUtil(String preferencePrefix) {
		this.PREFERENCE_PREFIX = preferencePrefix;
	}

	public final String ENABLE_PROJECT_PREFERENCES(EditorType et) {
		return PREFERENCE_PREFIX+et+".project.prefs.enabled";
	}

	public String getPreferenceName(ProblemType problemType) {
		return PREFERENCE_PREFIX+problemType.getId();
	}

	public ProblemSeverity getSeverity(IPreferenceStore prefs, ProblemType problemType) {
		String value = prefs.getString(getPreferenceName(problemType));
		try {
			if (StringUtil.hasText(value)) {
				return ProblemSeverity.valueOf(value);
			}
		} catch (Exception e) {
			//corrupted data in prefs store? Or maybe its just stale data from a different version
			// of STS. Ignore silently and use default value
		}
		return problemType.getDefaultSeverity();
	}

	public void setSeverity(IPreferenceStore prefs, ProblemType problemType, ProblemSeverity severity) {
		prefs.setValue(getPreferenceName(problemType), severity.toString());
	}

	public boolean projectPreferencesEnabled(IPreferenceStore projectPrefs, EditorType et) {
		return projectPrefs.getBoolean(ENABLE_PROJECT_PREFERENCES(et));
	}

	public void enableProjectPrefs(IPreferenceStore projectPrefs, EditorType et, boolean enable) {
		projectPrefs.setValue(ENABLE_PROJECT_PREFERENCES(et), enable);
	}

	public static void save(IPreferenceStore preferences) {
		if (preferences.needsSaving() && preferences instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) preferences).save();
			} catch (IOException e) {
				Log.log(e);
			}
		}
	}

}
