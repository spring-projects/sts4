/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.XmlBeansTestConf;
import org.springframework.ide.vscode.boot.test.DefinitionLinkAsserts;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

/**
 * @author Alex Boyko
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(XmlBeansTestConf.class)
public class XmlBeansHyperlinkTest {
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private DefinitionLinkAsserts definitionLinkAsserts;
	@Autowired private MockProjectObserver projectObserver;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private MavenJavaProject project;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", true);
		supportXML.put("hyperlinks", true);
		supportXML.put("scan-folders", "/src/main/");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(new Gson().toJsonTree(settings)));

		project = projects.mavenProject("test-xml-hyperlinks");
		
		harness.useProject(project);
		
		projectObserver.doWithListeners(l -> l.created(project));

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void testBeanClassHyperlink() throws Exception {
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someId\" class=\"u.t.r.SimpleObj\"></bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		definitionLinkAsserts.assertLinkTargets(editor, "u.t.r.SimpleObj", project, editor.rangeOf("u.t.r.SimpleObj", "u.t.r.SimpleObj"), "u.t.r.SimpleObj");
	}

	@Test
	public void testBeanPropertyNameHyperlink() throws Exception {
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someBean\" class=\"u.t.r.TestBean\"\n" +
				"<property name=\"age\" value=\"10\" />\n" +
				"</bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		definitionLinkAsserts.assertLinkTargets(editor, "age", project,
				editor.rangeOf("<property name=\"age\" value=\"10\" />", "age"),
				DefinitionLinkAsserts.method("u.t.r.TestBean", "setAge", "int"));
	}
	
	@Test
	public void testBeanPropertyNameFromSuperClassHyperlink() throws Exception {
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someBean\" class=\"u.t.r.TestBean\"\n" +
				"<property name=\"message\" value=\"Hello\" />\n" +
				"</bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		definitionLinkAsserts.assertLinkTargets(editor, "message", project,
				editor.rangeOf("<property name=\"message\" value=\"Hello\" />", "message"),
				DefinitionLinkAsserts.method("u.t.r.SuperTestBean", "setMessage", "java.lang.String"));
	}
	
	@Test
	public void testBeanRefHyperlink() throws Exception {
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someBean\" class=\"u.t.r.TestBean\"\n" +
				"<property name=\"simple\" ref=\"simpleObj\"></property>\n" +
				"</bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		Path rootContextFilePath = Paths.get(project.getLocationUri()).resolve("src/main/webapp/WEB-INF/spring/root-context.xml");
		Range targetRange = new Range(new Position(6,7), new Position(6, 21));
		LocationLink expectedLocation = new LocationLink(
				UriUtil.toUri(rootContextFilePath.toFile()).toString(),
				targetRange,
				targetRange,
				editor.rangeOf("name=\"simple\" ref=\"simpleObj\"", "simpleObj")
		);
		editor.assertLinkTargets("simpleObj", Collections.singleton(expectedLocation));
	}
	
	@Test
	public void testBeanRefNoHyperlink_FolderNotScanned() throws Exception {
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", true);
		supportXML.put("hyperlinks", true);
		supportXML.put("scan-folders", "  ");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(new Gson().toJsonTree(settings)));
		
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someBean\" class=\"u.t.r.TestBean\"\n" +
				"<property name=\"simple\" ref=\"simpleObj\"></property>\n" +
				"</bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		Path rootContextFilePath = Paths.get(project.getLocationUri()).resolve("src/main/webapp/WEB-INF/spring/root-context.xml");
		Location expectedLocation = new Location();
		expectedLocation.setUri(UriUtil.toUri(rootContextFilePath.toFile()).toString());
		expectedLocation.setRange(new Range(new Position(6,7), new Position(6, 21)));
		editor.assertNoLinkTargets("simpleObj");
	}
	
	@Test
	public void testBeanRefHyperlink_SpecifyScanFolderDifferently() throws Exception {
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", true);
		supportXML.put("hyperlinks", true);
		supportXML.put("scan-folders", "  src/main/  ");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(new Gson().toJsonTree(settings)));
		
		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someBean\" class=\"u.t.r.TestBean\"\n" +
				"<property name=\"simple\" ref=\"simpleObj\"></property>\n" +
				"</bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		Path rootContextFilePath = Paths.get(project.getLocationUri()).resolve("src/main/webapp/WEB-INF/spring/root-context.xml");
		Range targetRange = new Range(new Position(6,7), new Position(6, 21));
		LocationLink expectedLocation = new LocationLink(
				UriUtil.toUri(rootContextFilePath.toFile()).toString(),
				targetRange,
				targetRange,
				editor.rangeOf("name=\"simple\" ref=\"simpleObj\"", "simpleObj")
		);
		editor.assertLinkTargets("simpleObj", Collections.singleton(expectedLocation));
	}
	
	@Test
	public void testNoHyperlinkWhenXmlSupportOff() throws Exception {
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", false);
		supportXML.put("hyperlinks", true);
		supportXML.put("scan-folders", "src/main");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(new Gson().toJsonTree(settings)));

		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someId\" class=\"u.t.r.SimpleObj\"></bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		editor.assertNoLinkTargets("u.t.r.SimpleObj");
	}
	
	@Test
	public void testNoHyperlinkWhenHyperlinksOff() throws Exception {
		Map<String, Object> supportXML = new HashMap<>();
		supportXML.put("on", true);
		supportXML.put("hyperlinks", false);
		supportXML.put("scan-folders", "src/main");
		Map<String, Object> bootJavaObj = new HashMap<>();
		bootJavaObj.put("support-spring-xml-config", supportXML);
		Map<String, Object> settings = new HashMap<>();
		settings.put("boot-java", bootJavaObj);
		
		harness.getServer().getWorkspaceService().didChangeConfiguration(new DidChangeConfigurationParams(new Gson().toJsonTree(settings)));

		Path xmlFilePath = Paths.get(project.getLocationUri()).resolve("beans.xml");
		Editor editor = harness.newEditor(LanguageId.XML,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"xsi:schemaLocation=\"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
				
				"<bean id=\"someId\" class=\"u.t.r.SimpleObj\"></bean>\n" +
				"</beans>\n",
				UriUtil.toUri(xmlFilePath.toFile()).toString()
		);
		editor.assertNoLinkTargets("u.t.r.SimpleObj");
	}
}
