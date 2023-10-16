/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite;

import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import org.openrewrite.Recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class RecipesReflectConfigGenerator {
	public static final void main(String[] args) throws JsonIOException, IOException {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonArray array = new JsonArray();
		
		generatePojoJson().forEach(array::add);

		if (args.length > 0) {
			System.out.println("ARG: " + args[0] + " array size = " + array.size());
			FileWriter fileWriter = new FileWriter(args[0]);
			gson.toJson(array, fileWriter);
			fileWriter.flush();
			fileWriter.close();
		} else {
			System.out.println(gson.toJson(array));
		}
	}
	
	private static Stream<JsonObject> generatePojoJson() {
		ScanResult res = new ClassGraph()
				.enableClassInfo()
				.ignoreClassVisibility()
				.overrideClassLoaders(RecipesReflectConfigGenerator.class.getClassLoader())
				.scan();
		
		return res.getSubclasses(Recipe.class).stream().map(ci -> {
			JsonObject obj = new JsonObject();
			obj.addProperty("name", ci.getName());
			obj.addProperty("queryAllPublicConstructors", true);
			obj.addProperty("allDeclaredFields", true);
			obj.add("methods", noArgsConstructorOnly());
			return obj;
		});		
	}
	
	
	static JsonArray noArgsConstructorOnly() {
		JsonObject constr = new JsonObject();
		constr.addProperty("name", "<init>");
		constr.add("parameterTypes", new JsonArray());
		JsonArray methods = new JsonArray(1);
		methods.add(constr);
		return methods;
	}
}
