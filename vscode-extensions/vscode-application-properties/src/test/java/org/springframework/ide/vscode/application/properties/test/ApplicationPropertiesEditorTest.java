/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.ide.vscode.application.properties.reconcile.ApplicationPropertiesProblemType.PROP_DUPLICATE_KEY;
import static org.springframework.ide.vscode.project.harness.TestAsserts.assertContains;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.application.properties.ApplicationPropertiesLanguageServer;
import org.springframework.ide.vscode.application.properties.metadata.CachingValueProvider;
import org.springframework.ide.vscode.application.properties.metadata.PropertiesLoader;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.properties.editor.test.harness.AbstractPropsEditorTest;
import org.springframework.ide.vscode.properties.editor.test.harness.StyledStringMatcher;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;

/**
 * Boot App Properties Editor tests
 * 
 * @author Alex Boyko
 *
 */
public class ApplicationPropertiesEditorTest extends AbstractPropsEditorTest {
	
	private JavaProjectFinder javaProjectFinder;
	
	@Test public void testReconcileCatchesParseError() throws Exception {
		Editor editor = newEditor("key\n");
		editor.assertProblems("key|extraneous input");
	}
	
	public void linterRunsOnDocumentOpenAndChange() throws Exception {
		Editor editor = newEditor("key");

		editor.assertProblems("key|mismatched input");
		
		editor.setText(
				"problem\n" +
				"key=value\n" +
				"another"		
		);

		editor.assertProblems("problem|extraneous input", "another|mismatched input");
	}

	@Test public void testServerPortCompletion() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		assertCompletion("ser<*>", "server.port=<*>");
		assertCompletionDisplayString("ser<*>", "server.port : int");
	}

	@Test public void testLoggingLevelCompletion() throws Exception {
		data("logging.level", "java.util.Map<java.lang.String,java.lang.Object>", null, "Logging level per package.");
		assertCompletion("lolev<*>","logging.level.<*>");
	}

	@Test public void testListCompletion() throws Exception {
		data("foo.bars", "java.util.List<java.lang.String>", null, "List of bars in foo.");
		assertCompletion("foba<*>","foo.bars=<*>");
	}

	@Test public void testInetAddresCompletion() throws Exception {
		defaultTestData();
		assertCompletion("server.add<*>", "server.address=<*>");
	}

	@Test public void testStringArrayCompletion() throws Exception {
		data("spring.freemarker.view-names", "java.lang.String[]", null, "White list of view names that can be resolved.");
		data("some.defaulted.array", "java.lang.String[]", new String[] {"a", "b", "c"} , "Stuff.");

		assertCompletion("spring.freemarker.vn<*>", "spring.freemarker.view-names=<*>");
		assertCompletion("some.d.a<*>", "some.defaulted.array=<*>");
	}

	@Test public void testEmptyPrefixProposalsSortedAlpabetically() throws Exception {
		defaultTestData();
		Editor editor = newEditor("");
		List<CompletionItem> completions = editor.getCompletions();
		assertTrue(completions.size()>100); //should be many proposals
		String previous = null;
		for (CompletionItem c : completions) {
			String current = c.getLabel();
			if (previous!=null) {
				assertTrue("Incorrect order: \n   "+previous+"\n   "+current, previous.compareTo(current)<=0);
			}
			previous = current;
		}
	}

	@Test public void testValueCompletion() throws Exception {
		defaultTestData();
		assertCompletionsVariations("liquibase.enabled=<*>",
				"liquibase.enabled=false<*>",
				"liquibase.enabled=true<*>"
		);

		assertCompletionsVariations("liquibase.enabled:<*>",
				"liquibase.enabled:false<*>",
				"liquibase.enabled:true<*>"
		);

		assertCompletionsVariations("liquibase.enabled = <*>",
				"liquibase.enabled = false<*>",
				"liquibase.enabled = true<*>"
		);

		assertCompletionsVariations("liquibase.enabled   <*>",
				"liquibase.enabled   false<*>",
				"liquibase.enabled   true<*>"
		);

		assertCompletionsVariations("liquibase.enabled=f<*>",
				"liquibase.enabled=false<*>"
		);

		assertCompletionsVariations("liquibase.enabled=t<*>",
				"liquibase.enabled=true<*>"
		);

		assertCompletionsVariations("liquibase.enabled:f<*>",
				"liquibase.enabled:false<*>"
		);

		assertCompletionsVariations("liquibase.enabled:t<*>",
				"liquibase.enabled:true<*>"
		);

		assertCompletionsVariations("liquibase.enabled = f<*>",
				"liquibase.enabled = false<*>"
		);

		assertCompletionsVariations("liquibase.enabled = t<*>",
				"liquibase.enabled = true<*>"
		);

		assertCompletionsVariations("liquibase.enabled   t<*>",
				"liquibase.enabled   true<*>"
		);

		//one more... for special char like '-' in the name

		assertCompletionsVariations("liquibase.check-change-log-location=t<*>",
				"liquibase.check-change-log-location=true<*>"
		);
	}


	@Ignore @Test public void testHoverInfos() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"#foo\n" +
				"# bar\n" +
				"server.port=8080\n" +
				"logging.level.com.acme=INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		editor.assertHoverText("server.",
				"<b>server.port</b>"
		);
		//Case 2: an object/map property has extra text after the property name
		editor.assertHoverText("logging.", "<b>logging.level</b>");
	}

	@Ignore @Test public void testHoverInfosWithSpaces() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"#foo\n" +
				"# bar\n"+
				"\n" +
				"  server.port = 8080\n" +
				"  logging.level.com.acme = INFO\n"
		);
		//Case 1: an 'exact' match of the property is in the hover region
		editor.assertHoverText("server.",
				"<b>server.port</b>"
		);
		//Case 2: an object/map property has extra text after the property name
		editor.assertHoverText("logging.", "<b>logging.level</b>");
	}

	@Ignore @Test public void testHoverLongAndShort() throws Exception {
		data("server.port", INTEGER, 8080, "Port where server listens for http.");
		data("server.port.fancy", BOOLEAN, 8080, "Whether the port is fancy.");
		Editor editor = newEditor(
				"server.port=8080\n" +
				"server.port.fancy=true\n"
		);
		editor.assertHoverText("server.", "<b>server.port</b>");
		editor.assertHoverText("port.fa", "<b>server.port.fancy</b>");
	}


	@Test public void testPredefinedProject() throws Exception {
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		IType type = p.findType("demo.DemoApplication");
		assertNotNull(type);
	}

	@Test public void testEnableApt() throws Throwable {
		MavenJavaProject p = createPredefinedMavenProject("boot-1.2.0-properties-live-metadta");

		//Check some assumptions about the initial state of the test project (if these checks fail then
		// the test may be 'vacuous' since the things we are testing for already exist beforehand.
		Path metadataFile = p.getOutputFolder().resolve(PropertiesLoader.PROJECT_META_DATA_LOCATIONS[0]);
		assertTrue(metadataFile.toFile().isFile());
		assertContains("\"name\": \"foo.counter\"", Files.toString(metadataFile.toFile(), Charset.forName("UTF8")));
	}

	@Ignore @Test public void testHyperlinkTargets() throws Exception {
		System.out.println(">>> testHyperlinkTargets");
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);

		Editor editor = newEditor(
				"server.port=888\n" +
				"spring.datasource.login-timeout=1000\n" +
				"flyway.init-sqls=a,b,c\n"
		);

		editor.assertLinkTargets("server",
				"org.springframework.boot.autoconfigure.web.ServerProperties.setPort(Integer)"
		);
		editor.assertLinkTargets("data",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.hikariDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.tomcatDataSource()",
				"org.springframework.boot.autoconfigure.jdbc.DataSourceConfigMetadata.dbcpDataSource()"
		);
		editor.assertLinkTargets("flyway",
				"org.springframework.boot.autoconfigure.flyway.FlywayProperties.setInitSqls(List<String>)");
		System.out.println("<<< testHyperlinkTargets");
	}

	@Ignore @Test public void testHyperlinkTargetsLoggingLevel() throws Exception {
		System.out.println(">>> testHyperlinkTargetsLoggingLevel");
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		
		useProject(p);

		Editor editor = newEditor(
				"logging.level.com.acme=INFO\n"
		);
		editor.assertLinkTargets("level",
				"org.springframework.boot.logging.LoggingApplicationListener"
		);
		System.out.println("<<< testHyperlinkTargetsLoggingLevel");
	}

	@Test public void testReconcile() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server.port=8080\n" +
				"server.port.extracrap=8080\n" +
				"logging.level.com.acme=INFO\n" +
				"logging.snuggem=what?\n" +
				"bogus.no.good=true\n"
		);
		editor.assertProblems(
				".extracrap|Can't use '.' navigation",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);

	}

	@Test public void testReconcilePojoArray() throws Exception {
		IJavaProject p = createPredefinedMavenProject("boot-1.2.1-app-properties-list-of-pojo");
		
		useProject(p);
		assertNotNull(p.findType("demo.Foo"));

		Editor editor = newEditor(
				"token.bad.guy=problem\n"+
				"volder.foo.list[0].name=Kris\n" +
				"volder.foo.list[0].description=Kris\n" +
				"volder.foo.list[0].roles[0]=Developer\n"+
				"volder.foo.list[0]garbage=Grable\n"+
				"volder.foo.list[0].bogus=Bad\n"
		);

		//This is the more ambitious requirement but it is not implemented yet.
		editor.assertProblems(
				"token.bad.guy|unknown property",
				//'name' is ok
				//'description' is ok
				"garbage|'.' or '['",
				"bogus|has no property"
		);
	}

	@Test public void testPojoArrayCompletions() throws Exception {
		IJavaProject p = createPredefinedMavenProject("boot-1.2.1-app-properties-list-of-pojo");
		
		useProject(p);
		assertNotNull(p.findType("demo.Foo"));

		assertCompletionsVariations("volder.foo.l<*>", "volder.foo.list[<*>");
		assertCompletionsDisplayString("volder.foo.list[0].<*>",
				"name : String",
				"description : String",
				"roles : List<String>");

		assertCompletionsVariations("volder.foo.list[0].na<*>",
				"volder.foo.list[0].name=<*>"
		);
		assertCompletionsVariations("volder.foo.list[0].d<*>",
				"volder.foo.list[0].description=<*>"
		);
		assertCompletionsVariations("volder.foo.list[0].rl<*>",
				"volder.foo.list[0].roles=<*>"
		);
	}

	@Test public void testReconcileArrayNotation() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"borked=bad+\n" + //token problem, to make sure reconciler is working
				"security.user.role[0]=foo\n" +
				"security.user.role[${one}]=foo"
		);
		editor.assertProblems(
				"orked|unknown property"
				//no other problems
		);
	}

	@Test public void testReconcileArrayNotationError() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"security.user.role[bork]=foo\n" +
				"security.user.role[1=foo\n" +
				"security.user.role[1]crap=foo\n" +
				"server.port[0]=8888\n" +
				"spring.thymeleaf.view-names[1]=hello" //This is okay now. Boot handles this notation for arrays
		);
		editor.assertProblems(
				"bork|Integer",
				"[|matching ']'",
				"crap|'.' or '['",
				"[0]|Can't use '[..]'"
				//no other problems
		);
	}

	@Test public void testRelaxedNameReconciling() throws Exception {
		data("connection.remote-host", "java.lang.String", "service.net", null);
		data("foo-bar.name", "java.lang.String", null, null);
		Editor editor = newEditor(
				"bork=foo\n" +
				"connection.remote-host=alternate.net\n" +
				"connection.remoteHost=alternate.net\n" +
				"foo-bar.name=Charlie\n" +
				"fooBar.name=Charlie\n"
		);
		editor.assertProblems(
				"bork|unknown property"
				//no other problems
		);
	}

	@Test public void testRelaxedNameReconcilingErrors() throws Exception {
		//Tricky with relaxec names: the error positions have to be moved
		// around because the relaxed names aren't same length as the
		// canonical ids.
		data("foo-bar-zor.enabled", "java.lang.Boolean", null, null);
		Editor editor = newEditor(
				"fooBarZor.enabled=notBoolean\n" +
				"fooBarZor.enabled.subprop=true\n"
		);
		editor.assertProblems(
				"notBoolean|boolean",
				".subprop|Can't use '.' navigation"
		);
	}

	@Test public void testRelaxedNameContentAssist() throws Exception {
		data("foo-bar-zor.enabled", "java.lang.Boolean", null, null);
		assertCompletion("fooBar<*>", "foo-bar-zor.enabled=<*>");
	}

	@Test public void testReconcileValues() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server.port=badPort\n" +
				"liquibase.enabled=nuggels"
		);
		editor.assertProblems(
				"badPort|'int'",
				"nuggels|'boolean'"
		);
	}

	@Test public void testNoReconcileInterpolatedValues() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server.port=${port}\n" +
				"liquibase.enabled=nuggels"
		);
		editor.assertProblems(
				//no problem should be reported for ${port}
				"nuggels|'boolean'"
		);
	}

	@Test public void testReconcileValuesWithSpaces() throws Exception {
		defaultTestData();
		Editor editor = newEditor(
				"server.port  =   badPort\n" +
				"liquibase.enabled   nuggels  \n" +
				"liquibase.enabled   : snikkers"
		);
		editor.ignoreProblem(PROP_DUPLICATE_KEY); //ignore deliberate abuse of dups
		editor.assertProblems(
				"badPort|'int'",
				"nuggels|'boolean'",
				"snikkers|'boolean'"
		);
	}


	@Test public void testReconcileWithExtraSpaces() throws Exception {
		defaultTestData();
		//Same test as previous but with extra spaces to make things more confusing
		Editor editor = newEditor(
				"   server.port   =  8080  \n" +
				"\n" +
				"  server.port.extracrap = 8080\n" +
				" logging.level.com.acme  : INFO\n" +
				"logging.snuggem = what?\n" +
				"bogus.no.good=  true\n"
		);
		editor.assertProblems(
				".extracrap|Can't use '.' navigation",
				"snuggem|unknown property",
				"ogus.no.good|unknown property"
		);
	}

	@Test public void testEnumPropertyCompletionInsideCommaSeparateList() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));

		data("foo.colors", "java.util.List<demo.Color>", null, "A foonky list");

		//Completion requested right after '=' sign:
		assertCompletionsDisplayString("foo.colors=<*>", "red", "green", "blue");
		assertCompletionWithLabel("foo.colors=<*>", "red", "foo.colors=red<*>");
		assertCompletion("foo.colors=R<*>", "foo.colors=RED<*>");
		assertCompletion("foo.colors=g<*>", "foo.colors=green<*>");
		assertCompletion("foo.colors=B<*>", "foo.colors=BLUE<*>");

		//Completion requested after ','
		assertCompletionsDisplayString("foo.colors=red,<*>", "red", "green", "blue");
		assertCompletionWithLabel("foo.colors=red,<*>", "green", "foo.colors=red,green<*>");
		assertCompletion("foo.colors=RED,R<*>", "foo.colors=RED,RED<*>");
		assertCompletion("foo.colors=RED,G<*>", "foo.colors=RED,GREEN<*>");
		assertCompletion("foo.colors=RED,B<*>", "foo.colors=RED,BLUE<*>");
	}

	@Test public void testEnumPropertyCompletion() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");

		assertCompletion("foo.c<*>", "foo.color=<*>"); //Should add the '=' because enums are 'simple' values.

		assertCompletion("foo.color=R<*>", "foo.color=RED<*>");
		assertCompletion("foo.color=G<*>", "foo.color=GREEN<*>");
		assertCompletion("foo.color=B<*>", "foo.color=BLUE<*>");
		assertCompletionsDisplayString("foo.color=<*>",
				"red", "green", "blue"
		);
	}

	@Test public void testEnumPropertyReconciling() throws Exception {

		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));

		data("foo.color", "demo.Color", null, "A foonky colour");
		Editor editor = newEditor(
				"foo.color=BLUE\n"+
				"foo.color=RED\n"+
				"foo.color=GREEN\n"+
				"foo.color.bad=BLUE\n"+
				"foo.color=Bogus\n"
		);

		editor.ignoreProblem(PROP_DUPLICATE_KEY); //ignore deliberate abuse of dups
		
		editor.assertProblems(
				".bad|Can't use '.' navigation",
				"Bogus|Color"
		);
	}

	@Test public void testEnumMapValueCompletion() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));

		assertCompletionsVariations("foo.nam<*>",
				"foo.name-colors.<*>",
				"foo.color-names.<*>"
		);

		assertCompletionsDisplayString("foo.name-colors.something=<*>",
				"red", "green", "blue"
		);
		assertCompletionsVariations("foo.name-colors.something=G<*>", "foo.name-colors.something=GREEN<*>");
	}

	@Test public void testEnumMapValueReconciling() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		data("foo.name-colors", "java.util.Map<java.lang.String,demo.Color>", null, "Map with colors in its values");

		assertNotNull(p.findType("demo.Color"));

		Editor editor = newEditor(
				"foo.name-colors.jacket=BLUE\n" +
				"foo.name-colors.hat=RED\n" +
				"foo.name-colors.pants=GREEN\n" +
				"foo.name-colors.wrong=NOT_A_COLOR\n"
		);
		editor.assertProblems(
				"NOT_A_COLOR|Color"
		);
	}

	@Test public void testEnumMapKeyCompletion() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		data("foo.color-names", "java.util.Map<demo.Color,java.lang.String>", null, "Map with colors in its keys");
		data("foo.color-data", "java.util.Map<demo.Color,demo.ColorData>", null, "Map with colors in its keys, and pojo in values");
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		//Map Enum -> String:
		assertCompletionsVariations("foo.colnam<*>", "foo.color-names.<*>");
		assertCompletionsVariations("foo.color-names.<*>",
				"foo.color-names.blue=<*>",
				"foo.color-names.green=<*>",
				"foo.color-names.red=<*>"
		);
		assertCompletionsDisplayString("foo.color-names.<*>",
				"red : String", "green : String", "blue : String"
		);
		assertCompletionsVariations("foo.color-names.B<*>",
				"foo.color-names.BLUE=<*>"
		);

		//Map Enum -> Pojo:
		assertCompletionsVariations("foo.coldat<*>", "foo.color-data.<*>");
		assertCompletionsVariations("foo.color-data.<*>",
				"foo.color-data.blue.<*>",
				"foo.color-data.green.<*>",
				"foo.color-data.red.<*>"
		);
		assertCompletionsVariations("foo.color-data.B<*>",
				"foo.color-data.BLUE.<*>"
		);
		assertCompletionsDisplayString("foo.color-data.<*>",
				"blue : demo.ColorData", "green : demo.ColorData", "red : demo.ColorData"
		);
	}

	@Test public void testEnumMapKeyReconciling() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		Editor editor = newEditor(
				"foo.color-names.RED=Rood\n"+
				"foo.color-names.GREEN=Groen\n"+
				"foo.color-names.BLUE=Blauw\n" +
				"foo.color-names.NOT_A_COLOR=Wrong\n" +
				"foo.color-names.BLUE.bad=Blauw\n"
		);
		editor.assertProblems(
				"NOT_A_COLOR|Color",
				"BLUE.bad|Color" //because value type is not dotable the dots will be taken to be part of map key
		);
	}

	@Test public void testPojoCompletions() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		assertCompletion("foo.dat<*>", "foo.data.<*>");

		assertCompletionsDisplayString("foo.data.",
				"wavelen : double",
				"name : String",
				"next : demo.Color[BLUE, GREEN, RED]",
				"nested : demo.ColorData",
				"children : List<demo.ColorData>",
				"mapped-children : Map<String, demo.ColorData>",
				"color-children : Map<demo.Color[BLUE, GREEN, RED], demo.ColorData>",
				"tags : List<String>",
				"funky : boolean"
		);

		assertCompletionsVariations("foo.data.wav<*>", "foo.data.wavelen=<*>");
		assertCompletionsVariations("foo.data.nam<*>", "foo.data.name=<*>");
		assertCompletionsVariations("foo.data.nex<*>", "foo.data.next=<*>");
		assertCompletionsVariations("foo.data.nes<*>", "foo.data.nested.<*>");
		assertCompletionsVariations("foo.data.chi<*>",
				"foo.data.children[<*>",
				"foo.data.color-children.<*>", //fuzzy
				"foo.data.mapped-children.<*>" //fuzzy
		);
		assertCompletionsVariations("foo.data.tag<*>", "foo.data.tags=<*>");
		assertCompletionsVariations("foo.data.map<*>", "foo.data.mapped-children.<*>");
		assertCompletionsVariations("foo.data.col<*>", "foo.data.color-children.<*>");
	}

	@Test public void testPojoReconciling() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		Editor editor = newEditor(
			"foo.data.bogus=Something\n" +
			"foo.data.wavelen=3.0\n" +
			"foo.data.wavelen=not a double\n" +
			"foo.data.wavelen.more=3.0\n" +
			"foo.data.wavelen[0]=3.0\n"
		);
		editor.ignoreProblem(PROP_DUPLICATE_KEY); //ignore deliberate abuse of dups
		editor.assertProblems(
				"bogus|no property",
				"not a double|'double'",
				".more|Can't use '.' navigation",
				"[0]|Can't use '[..]' navigation"
		);
	}

	@Test public void testListOfAtomicCompletions() throws Exception {
		data("foo.slist", "java.util.List<java.lang.String>", null, "list of strings");
		data("foo.ulist", "java.util.List<Unknown>", null, "list of strings");
		data("foo.dlist", "java.util.List<java.lang.Double>", null, "list of doubles");
		assertCompletionsVariations("foo.u<*>", "foo.ulist[<*>");
		assertCompletionsVariations("foo.d<*>", "foo.dlist=<*>");
		assertCompletionsVariations("foo.sl<*>", "foo.slist=<*>");
	}

	@Test public void testMapKeyDotInterpretation() throws Exception {
		//Interpretation of '.' changes depending on the domain type (i.e. when domain type is
		//is a simple type got which '.' navigation is invalid then the '.' is 'eaten' by the key.

		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		data("atommap", "java.util.Map<java.lang.String,java.lang.Integer>", null, "map of atomic data");
		data("objectmap", "java.util.Map<java.lang.String,java.lang.Object>", null, "map of atomic object (recursive map)");
		data("enummap", "java.util.Map<java.lang.String,demo.Color>", null, "map of enums");
		data("pojomap", "java.util.Map<java.lang.String,demo.ColorData>", null, "map of pojos");

		Editor editor = newEditor(
				"atommap.something.with.dots=Vaporize\n" +
				"atommap.something.with.bracket[0]=Brackelate\n" +
				"objectmap.other.with.dots=Objectify\n" +
				"enummap.more.dots=Enumerate\n" +
				"pojomap.do.some.dots=Pojodot\n" +
				"pojomap.bracket.and.dots[1]=lala\n" +
				"pojomap.zozo[2]=lala\n"
		);
		editor.assertProblems(
				"Vaporize|'int'",
				"[0]|Can't use '[..]'",
				//objectmap okay
				"Enumerate|Color",
				"some|no property",
				"and|no property",
				"[2]|Can't use '[..]'"
		);

		assertCompletionsVariations("enummap.more.dots=R<*>",
				"enummap.more.dots=RED<*>",
				"enummap.more.dots=GREEN<*>" //fuzzy match: G(R)EEN
		);
	}

	@Test public void testMapKeyDotInterpretationInPojo() throws Exception {
		//Similar to testMapKeyDotInterpretation but this time maps are not attached to property
		// directly but via a pojo property

		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));
		assertNotNull(p.findType("demo.ColorData"));

		Editor editor = newEditor(
				"foo.color-names.BLUE.dot=Blauw\n"+
				"foo.color-data.RED.name=Good\n"+
				"foo.color-data.GREEN.bad=Bad\n"+
				"foo.color-data.GREEN.wrong[1]=Wrong\n"
		);
		editor.assertProblems(
				"BLUE.dot|Color", //dot is eaten so this is an error
				"bad|no property", //dot not eaten so '.bad' is accessing a property
				"wrong|no property"
		);

		assertCompletionsVariations("foo.color-data.RED.ch<*>",
				"foo.color-data.RED.children[<*>",
				"foo.color-data.RED.color-children.<*>",
				"foo.color-data.RED.mapped-children.<*>"
		);
	}

	@Test public void testEnumsInLowerCaseReconciling() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		Editor editor = newEditor(
				"simple.pants.size=NOT_A_SIZE\n"+
				"simple.pants.size=EXTRA_SMALL\n"+
				"simple.pants.size=extra-small\n"+
				"simple.pants.size=small\n"+
				"simple.pants.size=SMALL\n"
		);
		editor.ignoreProblem(PROP_DUPLICATE_KEY); //ignore deliberate abuse of dups
		editor.assertProblems(
				"NOT_A_SIZE|ClothingSize"
		);

		editor = newEditor(
				"foo.color-names.red=Rood\n"+
				"foo.color-names.green=Groen\n"+
				"foo.color-names.blue=Blauw\n" +
				"foo.color-names.not-a-color=Wrong\n" +
				"foo.color-names.blue.bad=Blauw\n"
		);
		editor.assertProblems(
				"not-a-color|Color",
				"blue.bad|Color" //because value type is not dotable the dots will be taken to be part of map key
		);

		editor = newEditor(
				"foo.color-data.red.next=green\n" +
				"foo.color-data.green.next=not a color\n" +
				"foo.color-data.red.bogus=green\n" +
				"foo.color-data.red.name=Rood\n"
		);
		editor.assertProblems(
				"not a color|Color",
				"bogus|no property"
		);
	}

	@Test public void testEnumsInLowerCaseContentAssist() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.ClothingSize"));

		data("simple.pants.size", "demo.ClothingSize", null, "The simple pant's size");

		assertCompletionsVariations("simple.pants.size=S<*>",
				"simple.pants.size=SMALL<*>",
				"simple.pants.size=EXTRA_SMALL<*>"
		);
		assertCompletionsVariations("simple.pants.size=s<*>",
				"simple.pants.size=small<*>",
				"simple.pants.size=extra-small<*>"
		);
		assertCompletionsVariations("simple.pants.size=ex<*>",
				"simple.pants.size=extra-large<*>",
				"simple.pants.size=extra-small<*>"
		);
		assertCompletionsVariations("simple.pants.size=EX<*>",
				"simple.pants.size=EXTRA_LARGE<*>",
				"simple.pants.size=EXTRA_SMALL<*>"
		);
		assertCompletionsDisplayString("foo.color=<*>", "red", "green", "blue");

		assertCompletionsVariations("foo.color-data.R<*>",
				"foo.color-data.RED.<*>",
				"foo.color-data.GREEN.<*>"
		);
		assertCompletionsVariations("foo.color-data.r<*>",
				"foo.color-data.red.<*>",
				"foo.color-data.green.<*>"
		);
		assertCompletionsVariations("foo.color-data.<*>",
				"foo.color-data.blue.<*>",
				"foo.color-data.green.<*>",
				"foo.color-data.red.<*>"
		);

		assertCompletionsVariations("foo.color-data.red.na<*>", "foo.color-data.red.name=<*>");
	}

	@Test public void testNavigationProposalAfterRelaxedPropertyName() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);

		assertCompletionsVariations("foo.colorData.b<*>", "foo.colorData.blue.<*>");
		assertCompletionsVariations("foo.colorData.red.na<*>", "foo.colorData.red.name=<*>");
	}

	@Test public void testValueProposalAssignedToRelaxedPropertyName() throws Exception {
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);

		data("relaxed-color", "demo.Color", null, "A soothing color");

		assertCompletion("relaxed-color=b<*>", "relaxed-color=blue<*>");
		assertCompletion("relaxedColor=b<*>", "relaxedColor=blue<*>");
	}

	/*
	 * TODO: Remove editor.setText(contents) after the call to deprecate(...) once property index listener mechanism is in place
	 */
	@Test public void testReconcileDeprecatedProperty() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		String contents = "# a comment\n"
				+ "error.path=foo\n";
		Editor editor = newEditor(contents);

		deprecate("error.path", "server.error.path", null);
		editor.setText(contents);
		editor.assertProblems(
				"error.path|Deprecated: Use 'server.error.path'"
				//no other problems
		);

		deprecate("error.path", "server.error.path", "This is old.");
		editor.setText(contents);
		editor.assertProblems(
				"error.path|Deprecated: Use 'server.error.path' instead. Reason: This is old."
				//no other problems
		);

		deprecate("error.path", null, "This is old.");
		editor.setText(contents);
		editor.assertProblems(
				"error.path|Deprecated: This is old."
				//no other problems
		);

		deprecate("error.path", null, null);
		editor.setText(contents);
		editor.assertProblems(
				"error.path|Deprecated!"
				//no other problems
		);

	}

	@Ignore @Test public void testDeprecatedPropertyCompletion() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		data("server.error.path", "java.lang.String", null, "Path of the error controller.");
		deprecate("error.path", "server.error.path", "This is old.");
		assertCompletionsDisplayString("error.pa<*>",
				"server.error.path : String", // should be first because it is not deprecated, even though it is not as good a pattern match
				"error.path : String"
		);
		//TODO: could we check that 'deprecated' completions are formatted with 'strikethrough font?
		assertStyledCompletions("error.pa<*>",
				StyledStringMatcher.plainFont("server.error.path : String"),
				StyledStringMatcher.strikeout("error.path")
		);
	}

	@Ignore @Test public void testDeprecatedPropertyHoverInfo() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		Editor editor = newEditor(
				"# a comment\n"+
				"error.path=foo\n"
		);

		deprecate("error.path", "server.error.path", null);
		editor.assertHoverText("path", "<s>error.path</s> -&gt; server.error.path");
		editor.assertHoverText("path", "<b>Deprecated!</b>");

		deprecate("error.path", "server.error.path", "This is old.");
		editor.assertHoverText("path", "<s>error.path</s> -&gt; server.error.path");
		editor.assertHoverText("path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, "This is old.");
		editor.assertHoverText("path", "<b>Deprecated: </b>This is old");

		deprecate("error.path", null, null);
		editor.assertHoverText("path", "<b>Deprecated!</b>");
	}

	@Ignore @Test public void testDeprecatedPropertyQuickfix() throws Exception {
		data("error.path", "java.lang.String", null, "Path of the error controller.");
		deprecate("error.path", "server.error.path", null);

		Editor editor = newEditor(
				"# a comment\n"+
				"error.path=foo\n"
		);

		Diagnostic problem = editor.assertProblem("error.path");
		CompletionItem fix = editor.assertFirstQuickfix(problem, "Change to 'server.error.path'");
		editor.apply(fix);
		editor.assertText(
				"# a comment\n"+
				"server.error.path<*>=foo\n"
		);
	}

	@Test public void testDeprecatedBeanPropertyReconcile() throws Exception {
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		Editor editor = newEditor(
				"# comment\n" +
				"foo.name=Old faithfull\n" +
				"foo.new-name=New and fancy\n" +
				"foo.alt-name=alternate\n"
		);
		editor.assertProblems(
				"name|Deprecated",
				"alt-name|Property 'alt-name' of type 'demo.Deprecater' is Deprecated: Use 'something.else' instead. Reason: No good anymore"
		);
	}

	@Ignore @Test public void testDeprecatedBeanPropertyCompletions() throws Exception {
		IJavaProject p = createPredefinedMavenProject("tricky-getters-boot-1.3.1-app");
		useProject(p);
		data("foo", "demo.Deprecater", null, "A Bean with deprecated properties");

		assertStyledCompletions("foo.nam<*>",
				StyledStringMatcher.plainFont("new-name : String"),
				StyledStringMatcher.strikeout("name"),
				StyledStringMatcher.strikeout("alt-name")
		);
	}

	@Test public void testCharsetCompletions() throws Exception {
		data("foobar.encoding", "java.nio.charset.Charset", null, "The charset-encoding to use for foobars");

		assertCompletions(
				"foobar.enco<*>"
				, // ==>
				"foobar.encoding=<*>"
		);

		assertCompletionWithLabel(
				"foobar.encoding=UT<*>"
				,
				"UTF-8"
				,
				"foobar.encoding=UTF-8<*>"
		);
	}

	@Test public void testLocaleCompletions() throws Exception {
		data("foobar.locale", "java.util.Locale", null, "Yada yada");

		assertCompletions(
				"foobar.loca<*>"
				, // ==>
				"foobar.locale=<*>"
		);

		assertCompletionWithLabel(
				"foobar.locale=en<*>"
				,
				"en_CA"
				,
				"foobar.locale=en_CA<*>"
		);
	}

	@Test public void testPropertyValueHintCompletions() throws Exception {
		//Test that 'value hints' work when property name is associated with 'value' hints.
		// via boot metadata.

		//TODO: this should also work when hints associated with a
		//  map property key
		//  map property value
		//  list property value

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletionsDisplayString(
				"spring.http.converters.preferred-json-mapper=<*>\n"
				, //=>
				"gson",
				"jackson"
		);
	}

	@Test public void testPropertyListHintCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		assertCompletion(
				"management.health.status.ord<*>"
				, //=>
				"management.health.status.order=<*>"
		);

		assertCompletionsDisplayString(
				"management.health.status.order=<*>"
				, //=>
				"DOWN",
				"OUT_OF_SERVICE",
				"UNKNOWN",
				"UP"
		);

		assertCompletionsDisplayString(
				"management.health.status.order=DOWN,<*>"
				, //=>
				"DOWN",
				"OUT_OF_SERVICE",
				"UNKNOWN",
				"UP"
		);
	}

	@Test public void testPropertyMapValueCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		assertCompletionsDisplayString(
				"logging.level.some: <*>"
				, // =>
				"trace",
				"debug",
				"info",
				"warn",
				"error",
				"fatal",
				"off"
		);

		assertCompletionsDisplayString(
				"logging.level.some.package: <*>"
				, // =>
				"trace",
				"debug",
				"info",
				"warn",
				"error",
				"fatal",
				"off"
		);
	}

	@Test public void testPropertyMapKeyCompletions() throws Exception {
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletionWithLabel(
				"logging.level.<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);

		assertCompletionWithLabel(
				"logging.level.r<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);

		assertCompletionWithLabel(
				"logging.level.ot<*>"
				, //==============
				"root : String",
				//=>
				"logging.level.root=<*>"
		);
	}

	@Test public void testHandleAsResourceContentAssist() throws Exception {
		//"name": "my.terms-and-conditions",
		//        "providers": [
		//                      {
		//                          "name": "handle-as",
		//                          "parameters": {
		//                              "target": "org.springframework.core.io.Resource"
		//                          }
		//                      }
		//                  ]
		data("my.terms-and-conditions", "java.lang.String", null, "Terms and Conditions text file")
		.provider("handle-as", "target", "org.springframework.core.io.Resource");

		assertCompletionsDisplayString(
				"my.terms-and-conditions=<*>"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);
	}

	@Test public void testHandleAsListContentAssist() throws Exception {
		data("my.tosses", "String[]", null, "A sequence of coin tosses")
			.provider("handle-as", "target", "java.lang.Boolean[]");

		assertCompletionsDisplayString(
				"my.tosses[0]=<*>"
				, // =>
				"true",
				"false"
		);
	}


	@Test public void test_STS_3335_reconcile_list_nested_in_Map_of_String() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("boot-1.3.3-sts-4335"));

		editor = newEditor(
				"test-map.test-list-object.color-list[0]=not-a-color\n"+
				"test-map.test-list-object.color-list[1]=RED\n"+
				"test-map.test-list-object.color-list[2]=GREEN\n"
		);
		editor.assertProblems(
				"not-a-color|Expecting 'com.wellsfargo.lendingplatform.web.config.Color"
		);

		editor = newEditor(
				"test-map.test-list-object.string-list[0]=not-a-color\n"+
				"test-map.test-list-object.string-list[1]=RED\n"+
				"test-map.test-list-object.string-list[2]=GREEN\n"
		);
		editor.assertProblems(/*NONE*/);
	}


	@Test public void test_STS_3335_completions_list_nested_in_Map_of_String() throws Exception {
		useProject(createPredefinedMavenProject("boot-1.3.3-sts-4335"));

		assertCompletions(
				"test-map.some-string-key.col<*>"
				, // =>
				"test-map.some-string-key.color-list=<*>"
		);

		assertCompletionsDisplayString(
				"test-map.some-string-key.color-list[0]=<*>\n"
				, // =>
				"red", "green", "blue"
		);

		assertCompletionsDisplayString(
				"test-map.some-string-key.color-list[0]=<*>\n"
				, // =>
				"red", "green", "blue"
		);
	}

	@Test public void testSimpleResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");

		assertCompletion(
				"nicer<*>\n"
				,// =>
				"my.nice.resource=<*>\n"
		);

		assertCompletionsDisplayString(
				"my.nice.resource=<*>\n"
				, // =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);
	}

	@Test public void testClasspathResourceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));

		data("my.nice.resource", "org.springframework.core.io.Resource", null, "A very nice resource.");
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");

		assertCompletionsDisplayString(
				"my.nice.resource=classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		//Test 'list item' context:

		assertCompletionsDisplayString(
				"my.nice.list[0]=<*>"
				,// =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
		);

		assertCompletionsDisplayString(
				"my.nice.list[0]=classpath:app<*>\n"
				,// =>
				"classpath:application.properties",
				"classpath:application.yml"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=classpath:app<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=classpath:application.yml<*>\n"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=  classpath:app<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=  classpath:application.yml<*>\n"
		);

		assertCompletionWithLabel(
				"my.nice.list[0]=classpath:<*>\n"
				,// ==========
				"classpath:application.yml"
				, // =>
				"my.nice.list[0]=classpath:application.yml<*>\n"
		);

		//Test 'raw node' context

		// do we find resources in sub-folders too?
		assertCompletionWithLabel(
				"my.nice.resource=classpath:word<*>\n"
				,//===============
				"classpath:stuff/wordlist.txt"
				,// =>
				"my.nice.resource=classpath:stuff/wordlist.txt<*>\n"
		);
	}

	@Test public void testClasspathResourceCompletionInCommaList() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		data("my.nice.list", "java.util.List<org.springframework.core.io.Resource>", null, "A nice list of resources.");
		data("my.nice.array", "org.springframework.core.io.Resource[]", null, "A nice array of resources.");

		for (String kind : ImmutableList.of("list", "array")) {
			assertCompletionWithLabel(
				"my.nice."+kind+"=classpath:<*>"
				,//===========
				"classpath:stuff/wordlist.txt"
				,//=>
				"my.nice."+kind+"=classpath:stuff/wordlist.txt<*>"
			);

			assertCompletionsDisplayString(
				"my.nice."+kind+"=<*>"
				,// =>
				"classpath:",
				"classpath*:",
				"file:",
				"http://",
				"https://"
			);

			assertCompletionWithLabel(
				"my.nice."+kind+"=classpath:stuff/wordlist.txt,classpath:app<*>"
				,//===========
				"classpath:application.yml"
				,//=>
				"my.nice."+kind+"=classpath:stuff/wordlist.txt,classpath:application.yml<*>"
			);
		}
	}

	@Ignore @Test public void testClassReferenceCompletion() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);

		useProject(createPredefinedMavenProject("empty-boot-1.3.0-with-mongo"));

		assertCompletion(
				"spring.data.mongodb.field-na<*>"
				, // =>
				"spring.data.mongodb.field-naming-strategy=<*>"
		);

		assertCompletionsDisplayString(
			"spring.data.mongodb.field-naming-strategy=<*>"
			, // =>
			"org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy",
			"org.springframework.data.mapping.model.CamelCaseSplittingFieldNamingStrategy",
			"org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy",
			"org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy"
		);

		assertCompletionWithLabel(
			"spring.data.mongodb.field-naming-strategy=<*>"
			, //=====
			"org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy"
			, //=>
			"spring.data.mongodb.field-naming-strategy=org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy<*>"
		);

		//Test what happens when 'target' type isn't on the classpath:
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-app"));
		assertCompletionsDisplayString(
			"spring.data.mongodb.field-naming-strategy=<*>"
			// =>
			/*NONE*/
		);
	}

	@Ignore @Test public void testClassReferenceInValueLink() throws Exception {
		Editor editor;
		useProject(createPredefinedMavenProject("empty-boot-1.3.0-with-mongo"));

		editor = newEditor(
				"#stuff\n" +
				"spring.data.mongodb.field-naming-strategy=org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy\n" +
				"#more stuff"
		);
		editor.assertLinkTargets("org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy", "org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy");

		//Linking should also work for types that aren't valid based on the constraints
		editor = newEditor(
				"#stuff\n" +
				"spring.data.mongodb.field-naming-strategy=java.lang.String\n" +
				"#more stuff"
		);
		editor.assertLinkTargets("java.lang.String", "java.lang.String");
	}

	@Test public void testCommaListReconcile() throws Exception {
		Editor editor;
		IJavaProject p = createPredefinedMavenProject("enums-boot-1.3.2-app");
		
		useProject(p);
		assertNotNull(p.findType("demo.Color"));

		data("my.colors", "java.util.List<demo.Color>", null, "Ooh! nice colors!");

//		editor = newEditor(
//				"#comment\n" +
//				"my.colors=RED, green, not-a-color , BLUE"
//		);
//		editor.assertProblems(
//				"not-a-color|demo.Color"
//		);

		editor = newEditor(
				"my.colors=\\\n" +
				"	red,\\\n" +
				"	green,\\\n" +
				"	blue\n"
		);
		editor.assertProblems(/*no problems*/);

		editor = newEditor(
				"my.colors=\\\n" +
				"	bad,\\\n" +
				"	green,\\\n" +
				"	blue\n"
		);
		editor.assertProblems( "bad|demo.Color");

		editor = newEditor(
				"my.colors=\\\n" +
				"	bad , \\\n" +
				"	green,\\\n" +
				"	blue\n"
		);
		editor.assertProblems( "bad|demo.Color");

		editor = newEditor(
				"my.colors=\\\n" +
				"	red , \\\n" +
				"	green,\\\n" +
				"	bad\n"
		);
		editor.assertProblems( "bad|demo.Color");

		editor = newEditor(
				"my.colors=\\\n" +
				"	red , \\\n" +
				"	green,\\\n" +
				"	bad   \n"
		);
		editor.assertProblems( "bad|demo.Color");

		editor = newEditor(
				"my.colors=red,\n"
		);
		editor.assertProblems( ",|demo.Color");


		editor = newEditor(
				"my.colors=red, \n"
		);
		editor.assertProblems( " |demo.Color");
	}

	@Test public void testReconcileDuplicateKey() throws Exception {
		Editor editor;
		data("some.property", "java.lang.String", null, "yada");
		data("some.other.property", "java.lang.String", null, "yada");

		editor = newEditor(
				"#comment\n" +
				"some.property=stuff\n" +
				"some.other.property=stuff\n" +
				"some.property=different stuff\n"
		);
		editor.assertProblems(
				"some.property|Duplicate",
				"some.property|Duplicate"
		);

		editor = newEditor(
				"#comment\n" +
				"some.property = stuff\n" +
				"some.other.property=stuff\n" +
				"some.property: different stuff\n" +
				"some.other.property=stuff\n" +
				"some.property: different stuff\n"
		);
		editor.assertProblems(
				"some.property|Duplicate",
				"some.other.property|Duplicate",
				"some.property|Duplicate",
				"some.other.property|Duplicate",
				"some.property|Duplicate"
		);
	}

	@Test public void test_PT_119352965() throws Exception {
		data("some.property", "java.lang.String", null, "Some property to test stuff")
		.valueHint("SOMETHING", "A value for something")
		.valueHint("ALTERNATE", "An alternative value");
		data("some.other.property", "java.lang.String", null, "Another property to test stuff");

		assertCompletionWithLabel(
				"some.property=SOMETHING\n" +
				"<*>"
				, // ===============
				"some.other.property : String"
				, // =>
				"some.property=SOMETHING\n" +
				"some.other.property=<*>"
		);
	}

	@Ignore @Test public void testEnumJavaDocShownInValueContentAssist() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		assertCompletionWithInfoHover(
				"my.background=<*>"
				, // ==========
				"red"
				, // ==>
				"Hot and delicious"
		);
	}

	@Ignore @Test public void testEnumJavaDocShownInValueHover() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		Editor editor;

		editor = newEditor(
				"my.background: RED"
		);
		editor.assertIsHoverRegion("RED");
		editor.assertHoverContains("RED", "Hot and delicious");

		editor = newEditor(
				"my.background=red"
		);
		editor.assertHoverContains("red", "Hot and delicious");
	}


	@Ignore @Test public void testEnumInValueLink() throws Exception {
		useProject(createPredefinedMavenProject("enums-boot-1.3.2-app"));
		data("my.background", "demo.Color", null, "Color to use as default background.");

		Editor editor;

		editor = newEditor(
				"my.background: RED"
		);
		editor.assertLinkTargets("RED", "demo.Color.RED");

		editor = newEditor(
				"my.background=red"
		);
		editor.assertLinkTargets("red", "demo.Color.RED");
	}

	@Override
	protected SimpleLanguageServer newLanguageServer() {
		ApplicationPropertiesLanguageServer server = new ApplicationPropertiesLanguageServer(md.getIndexProvider(), typeUtilProvider, javaProjectFinder);
		server.setMaxCompletionsNumber(-1);
		return server;
	}

	/**
	 * Like 'assertCompletionsBasic' but places the 'textBefore' in a context
	 * with other text around it... trying several different variations of
	 * text before and after the 'interesting' line.
	 */
	public void assertCompletionsVariations(String textBefore, String... expectTextAfter) throws Exception {
		//Variation 1: by itself
		assertCompletions(textBefore, expectTextAfter);
		//Variation 2: comment text before and after
		assertCompletions("#comment\n"+textBefore+"\n#comment", wrap("#comment\n", expectTextAfter, "\n#comment"));
		//Variation 3: empty lines of text before and after
		assertCompletions("\n"+textBefore+"\n\n", wrap("\n", expectTextAfter, "\n\n"));
		//Variation 3.b: empty lines of text before and single newline after
		assertCompletions("\n"+textBefore+"\n", wrap("\n", expectTextAfter, "\n"));
		//Variation 4: property assignment before and after
		assertCompletions("foo=bar\n"+textBefore+"\nnol=brol", wrap("foo=bar\n", expectTextAfter, "\nnol=brol"));
	}
	
	private String[] wrap(String before, String[] middle, String after) {
		//"\n"+expectTextAfter+"\n\n"
		String[] result = new String[middle.length];
		for (int i = 0; i < result.length; i++) {
			result[i] =  before+middle[i]+after;
		}
		return result;
	}
	
}
