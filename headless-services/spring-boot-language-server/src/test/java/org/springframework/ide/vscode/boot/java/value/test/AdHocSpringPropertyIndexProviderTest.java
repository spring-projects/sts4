/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.value.test.MockProjects.MockProject;
import org.springframework.ide.vscode.boot.metadata.AdHocSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocumentSaveChange;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class AdHocSpringPropertyIndexProviderTest {

	private MockProjects projects = new MockProjects();
	private MockDocumentEvents documents = new MockDocumentEvents();

	@Test
	public void parseProperties() throws Exception {
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.properties",
				"some-adhoc-foo=somefoo\n" +
				"some-adhoc-bar=somebar\n"
		);
		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, null, documents);

		assertProperties(indexer.getIndex(project),
				//alphabetic order
				"some-adhoc-bar",
				"some-adhoc-foo"
		);
	}

	@Test
	public void parseYamlWithList() throws Exception {
		//Note: the LoggerNameProvider implementation relies on this behavior
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.yml",
			"from-yaml:\n" +
			"  adhoc:\n" +
			"    - one\n" +
			"    - two\n"
		);
		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, null, documents);

		assertProperties(indexer.getIndex(project),
				"from-yaml.adhoc"
		);

	}

	@Test
	public void parseYaml() throws Exception {
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.yml",
			"from-yaml:\n" +
			"  adhoc:\n" +
			"    foo: somefoo\n" +
			"    bar: somebar\n"
		);
		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, null, documents);

		assertProperties(indexer.getIndex(project),
				//alphabetic order
				"from-yaml.adhoc.bar",
				"from-yaml.adhoc.foo"
		);
	}

	@Test
	public void respondsToClasspathChanges() throws Exception {
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.properties",
				"initial-property=somefoo\n"
		);

		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, null, documents);

		assertProperties(indexer.getIndex(project),
				"initial-property"
		);

		project.ensureFile("new-sourcefolder/application.properties", "new-property=whatever");
		assertProperties(indexer.getIndex(project),
				"initial-property"
		);

		project.createSourceFolder("new-sourcefolder");
		assertProperties(indexer.getIndex(project),
				"initial-property",
				"new-property"
		);
	}

	@Test
	public void respondsToFileChanges() throws Exception {
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.properties",
				"initial-property=somefoo\n"
		);

		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, projects.fileObserver, documents);

		assertProperties(indexer.getIndex(project),
				"initial-property"
		);

		project.ensureFile("src/main/resources/application.properties", "from-properties=whatever");
		assertProperties(indexer.getIndex(project),
				"from-properties"
		);

		project.ensureFile("src/main/resources/application.yml", "from-yaml: whatever");
		assertProperties(indexer.getIndex(project),
				"from-properties",
				"from-yaml"
		);
	}

	@Test
	public void respondsToDocumentSave() throws Exception {
		MockProject project = projects.create("test-project");
		project.ensureFile("src/main/resources/application.properties",
				"initial-property=somefoo\n"
		);

		AdHocSpringPropertyIndexProvider indexer = new AdHocSpringPropertyIndexProvider(projects.finder, projects.observer, projects.fileObserver, documents);

		assertProperties(indexer.getIndex(project),
				"initial-property"
		);

		File propsFile = project.ensureFileNoEvents("src/main/resources/application.properties", "from-properties=whatever");
		assertProperties(indexer.getIndex(project),
				"initial-property" //not changed yet because didn't fire change events.
		);
		documents.fire(new TextDocumentSaveChange(new TextDocument(propsFile.toURI().toString(), LanguageId.BOOT_PROPERTIES)));
		assertProperties(indexer.getIndex(project),
				"from-properties"
		);

		project.ensureFileNoEvents("src/main/resources/application.yml", "from-yaml: whatever");
		assertProperties(indexer.getIndex(project),
				"from-properties"
		);
		documents.fire(new TextDocumentSaveChange(new TextDocument(propsFile.toURI().toString(), LanguageId.BOOT_PROPERTIES_YAML)));
		assertProperties(indexer.getIndex(project),
				"from-properties",
				"from-yaml"
		);
	}

	private void assertProperties(FuzzyMap<PropertyInfo> index, String... expectedProps) {
		StringBuilder foundProps = new StringBuilder();
		for (PropertyInfo p : index) {
			foundProps.append(p.getId()+"\n");
		}
		StringBuilder expecteds = new StringBuilder();
		for (String string : expectedProps) {
			expecteds.append(string+"\n");
		}
		assertEquals(expecteds.toString(), foundProps.toString());
	}

}
