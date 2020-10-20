/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public abstract class ProblemSeverityPreferityPageFromMetadata extends AbstractProblemSeverityPreferencesPage {

	public static Map<String, ProblemTypeData[]> readFromFile(File metadataFile) throws FileNotFoundException, IOException {
		Gson gson = new Gson();
		TypeToken<Map<String, ProblemTypeData[]>> tt = new TypeToken<Map<String,ProblemTypeData[]>>() {};
		try (Reader json = new FileReader(metadataFile)) {
			return gson.fromJson(json, tt.getType());
		}
	}

	public static class ProblemTypeData implements ProblemType {
		String code;
		String label;
		String description;
		String defaultSeverity;

		public ProblemTypeData() {}

		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}

		public ProblemSeverity getDefaultSeverity() {
			return ProblemSeverity.valueOf(defaultSeverity);
		}

		public void setDefaultSeverity(String defaultSeverity) {
			this.defaultSeverity = defaultSeverity;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String getId() {
			return code;
		}
	}

	private ProblemType[] problemTypes;

	public ProblemSeverityPreferityPageFromMetadata(ProblemSeverityPreferencesUtil util, ProblemTypeData[] problemTypeJsonData) {
		super(util);
		this.problemTypes = problemTypeJsonData;
	}

	@Override
	protected List<ProblemType> getProblemTypes() {
		return Arrays.asList(problemTypes);
	}

	@Override
	protected String getEnableProjectPreferencesKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
