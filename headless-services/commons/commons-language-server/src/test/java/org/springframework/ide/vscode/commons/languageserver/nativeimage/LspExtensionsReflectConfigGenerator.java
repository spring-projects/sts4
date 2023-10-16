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
package org.springframework.ide.vscode.commons.languageserver.nativeimage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class LspExtensionsReflectConfigGenerator {
	
	public static final void main(String[] args) throws JsonIOException, IOException {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonArray array = new JsonArray();

		generateLspExtensionsInterfacesJson().forEach(array::add);
		generateLspExtensionsPojoJson().forEach(array::add);
		
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

	private static Stream<JsonObject> generateLspExtensionsPojoJson() {
		ScanResult res = new ClassGraph().ignoreParentClassLoaders().enableMethodInfo().acceptPackages("org.springframework.ide.vscode.commons.protocol").scan();
		return res.getAllStandardClasses().stream().map(ci -> {
			JsonObject obj = new JsonObject();
			obj.addProperty("name", ci.getName());
			obj.addProperty("allDeclaredFields", true);
			obj.addProperty("allPublicConstructors", true);
			return obj;
		});
	}
	
	private static Stream<JsonObject> generateLspExtensionsInterfacesJson() {
		ScanResult res = new ClassGraph().ignoreParentClassLoaders().enableMethodInfo().acceptPackages("org.springframework.ide.vscode.commons.protocol").scan();
		return res.getAllInterfaces().stream().map(ci -> {
			JsonObject obj = new JsonObject();
			obj.addProperty("name", ci.getName());
			obj.addProperty("queryAllDeclaredMethods", true);
			obj.addProperty("queryAllPublicMethods", true);
			return obj;
		});
	}

}
