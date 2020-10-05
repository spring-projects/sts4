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
package org.springframework.ide.vscode.boot.test;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.springframework.ide.vscode.boot.java.SpringJavaProblemType;
import org.springframework.ide.vscode.boot.properties.reconcile.ApplicationPropertiesProblemType;
import org.springframework.ide.vscode.boot.yaml.reconcile.ApplicationYamlProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.util.JSONCursor;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Helper that dumps out ProblemTypes to a json file. This file can 
 * be used (how?) to drive problem severity configuration ui.
 * <p>
 * Run this from Eclipse simply by selecting this file and do
 * "Run As >> Java Application"
 */
public class ProblemTypesToJson {

	public static final String packageJsonPath = "../../vscode-extensions/vscode-spring-boot/package.json";
	public static final String resourcesPath = "src/main/resources";
	public static final String metaDataFileName = "problem-types.json";
	
	public static class ProblemTypeData {
		String code;
		String label;
		String description;
		String defaultSeverity;
		
		public ProblemTypeData() {}
		
		public ProblemTypeData(ProblemType type) {
			this.code = type.getCode();
			this.description = type.getDescription();
			this.defaultSeverity =type.getDefaultSeverity().toString();
			this.label = type.getLabel();
		}
		
		public ProblemTypeData(String defaultSeverity) {
			super();
			this.setDefaultSeverity(defaultSeverity);
		}

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

		public String getDefaultSeverity() {
			return defaultSeverity;
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
	}

	Map<String, ProblemTypeData[]> problemTypes = new HashMap<>();

	public static void main(String[] args) throws Exception {
		ProblemTypesToJson writer = new ProblemTypesToJson();
		writer.problemsFor("application-yaml", ApplicationYamlProblemType.values());
		writer.problemsFor("application-properties", ApplicationPropertiesProblemType.values());
		writer.problemsFor("java", SpringJavaProblemType.values());
		writer.dump();
		
		
		writer.updatePackageJson(new File(packageJsonPath), "spring-boot.ls.problem");
	}

	private void dump() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(problemTypes);
		FileUtils.writeStringToFile(new File(resourcesPath+"/"+metaDataFileName), json);
	}

	private void problemsFor(String string, ProblemType[] values) {
		ProblemTypeData[] data = new ProblemTypeData[values.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new ProblemTypeData(values[i]);
		}
		problemTypes.put(string, data);
	}

	public ProblemTypesToJson read() throws IOException {
		try (InputStream input = this.getClass().getResourceAsStream("/"+metaDataFileName)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			TypeToken<Map<String, ProblemTypeData[]>> tt = new TypeToken<Map<String, ProblemTypeData[]>>() {};
			problemTypes = gson.fromJson(new InputStreamReader(input), tt.getType());
		}
		return this;
	}

	public void validate(String name, ProblemType[] actualProblemTypes) {
		ProblemTypeData[] metadataProblemTypes = problemTypes.get(name);
		Set<String> metadataHas = Stream.of(metadataProblemTypes).map(x -> x.getCode()).collect(Collectors.toSet());
		Set<String> actualHas = Stream.of(actualProblemTypes).map(x-> x.getCode()).collect(Collectors.toSet());
		for (String string : actualHas) {
			if (!metadataHas.contains(string)) {
				throw new IllegalStateException("No metadata for "+string);
			}
		}
		for (String string : metadataHas) {
			if (!actualHas.contains(string)) {
				throw new IllegalStateException("Metadata for non-existent problem type "+string);
			}
		}
		
		for (int i = 0; i < metadataProblemTypes.length; i++) {
			ProblemTypeData metadata = metadataProblemTypes[i];
			ProblemType actual = actualProblemTypes[i];
			assertEquals(actual.getCode(), metadata.getCode());
			assertEquals(actual.getDescription(), metadata.getDescription());
			assertEquals(actual.getDefaultSeverity().toString(), metadata.getDefaultSeverity());
			assertEquals(actual.getLabel(), metadata.getLabel());
		}
	}

	public void updatePackageJson(File packageJsonFile, String propertyPrefix) throws Exception {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.serializeNulls()
				.create();
		JsonElement parsed = gson.fromJson(new FileReader(packageJsonFile), JsonElement.class);
		JsonObject configProps = (JsonObject) YamlPath.fromProperty("contributes.configuration.properties").traverse(new JSONCursor(parsed)).target;
		Set<String> removeProps = configProps.keySet().stream().filter(s -> s.startsWith(propertyPrefix)).collect(Collectors.toSet());
		for (String rp : removeProps) {
			configProps.remove(rp);
		}
		JsonArray severities = new JsonArray();
		for (ProblemSeverity severity : ProblemSeverity.values()) {
			severities.add(severity.name());
		}
		for (Entry<String, ProblemTypeData[]> e : problemTypes.entrySet()) {
			String editorType = e.getKey();
			for (ProblemTypeData data : e.getValue()) {
				JsonObject schema = new JsonObject();
				schema.addProperty("type", "string");
				schema.addProperty("default", data.defaultSeverity);
				schema.addProperty("description", data.description);
				schema.add("enum", severities);
				configProps.add(propertyPrefix+"."+editorType+"."+data.code, schema);
			}
		}
		
		String newContent = gson.toJson(parsed);
		System.out.println(newContent);
		FileUtils.writeStringToFile(packageJsonFile, newContent);
	}
	
}
