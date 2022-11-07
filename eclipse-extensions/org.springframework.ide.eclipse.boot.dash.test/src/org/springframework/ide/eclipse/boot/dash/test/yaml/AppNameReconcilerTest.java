/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.yaml;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameAnnotation;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameAnnotationModel;
import org.springframework.ide.eclipse.boot.dash.cf.deployment.AppNameReconciler;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Tests for application name reconciler
 *
 * @author Alex Boyko
 *
 */
public class AppNameReconcilerTest {

	private void testAppNames(String manifest, String...expectedAppNames) throws Exception {
		AppNameReconciler reconciler = new AppNameReconciler(new YamlASTProvider(new Yaml(new SafeConstructor())));
		AppNameAnnotationModel annotationModel = new AppNameAnnotationModel("test");
		Document doc = new Document(manifest);
		reconciler.reconcile(doc, annotationModel, new NullProgressMonitor());
		HashSet<String> actualAppNames = new HashSet<>();
		for (Iterator<?> itr = annotationModel.getAnnotationIterator(); itr.hasNext();) {
			Object o = itr.next();
			if (o instanceof AppNameAnnotation) {
				actualAppNames.add(((AppNameAnnotation)o).getText());
			}
		}
		assertEquals(new HashSet<>(Arrays.asList(expectedAppNames)), actualAppNames);
	}

	@Test
	public void testSingleAppName() throws Exception {
		testAppNames(
				"applications:\n" +
				"- name: app\n" +
				"  memory: 512M\n",
			"app");
	}

	@Test
	public void testMultipleAppNames() throws Exception {
		testAppNames(
				"applications:\n" +
				"- name: app\n" +
				"  memory: 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n",
			"app", "anotherApp", "someApp");
	}

	@Test
	public void testNoAppName() throws Exception {
		testAppNames(
				"applications:\n" +
				"  memory: 512M\n");
	}

	@Test
	public void testNoAppDueToSyntaxErrorNames() throws Exception {
		testAppNames(
				"applications:\n" +
				"- name: app\n" +
				"  memory 512M\n" +
				"- name: anotherApp\n" +
				"  memory: 512M\n" +
				"- name: someApp\n" +
				"  memory: 512M\n");
	}

}
