package org.springframework.ide.vscode.boot.properties.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.application.properties.metadata.CachingValueProvider;
import org.springframework.ide.vscode.application.properties.metadata.LoggerNameProvider;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class LoggerNameProviderTest {

	private static final String[] JBOSS_RESULTS = {
			"com.fasterxml.jackson.databind.jsonFormatVisitors", //1
//			"org.jboss", //2
			"org.jboss.logging", //3
			"org.jboss.logging.JBossLogManagerLogger", //4
			"org.jboss.logging.JBossLogManagerProvider", //5
			"org.jboss.logging.JBossLogRecord", //6
			"org.springframework.instrument.classloading.jboss", //7
			"org.springframework.instrument.classloading.jboss.JBossClassLoaderAdapter", //8
			"org.springframework.instrument.classloading.jboss.JBossLoadTimeWeaver", //9
			"org.springframework.instrument.classloading.jboss.JBossMCAdapter", //10
			"org.springframework.instrument.classloading.jboss.JBossMCTranslatorAdapter", //11
			"org.springframework.instrument.classloading.jboss.JBossModulesAdapter" //12
	};
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private MavenJavaProject project;

	@Before
	public void setup() throws Exception {
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);
		project = projects.mavenProject("tricky-getters-boot-1.3.1-app");
	}

	@After
	public void teardown() throws Exception {
		CachingValueProvider.restoreDefaults();
	}

	@Test
	public void directResults() throws Exception {
		LoggerNameProvider p = new LoggerNameProvider();
		String query = "jboss";
		List<String> directQueryResults = getResults(p, query);

		dumpResults("jboss - DIRECT", directQueryResults);

		/*
		 * Commented out due to search results from JDK present
		 */
//		assertElements(directQueryResults, JBOSS_RESULTS);
		assertElementsAtLeast(directQueryResults, JBOSS_RESULTS);
	}

	@Test
	public void cachedResults() throws Exception {
		LoggerNameProvider p = new LoggerNameProvider();
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			String query = "jboss";
			List<String> directQueryResults = getResults(p, query);

			/*
			 * Commented out due to search results from JDK present
			 */
//			assertElements(directQueryResults, JBOSS_RESULTS);
			assertElementsAtLeast(directQueryResults, JBOSS_RESULTS);

			long duration = System.currentTimeMillis() - startTime;
			System.out.println(i+": "+duration+" ms");
		}
	}

	@Test
	public void incrementalResults() throws Exception {
		String fullQuery = "jboss";

		CachingValueProvider p = new LoggerNameProvider();
		for (int i = 0; i <= fullQuery.length(); i++) {
			String query = fullQuery.substring(0, i);
			List<String> results = getResults(p, query);
			dumpResults(query, results);
			if (i==fullQuery.length()) {
				System.out.println("Verifying final result!");
				//Not checking for exact equals because... quircks of JDT search engine means it
				// will actually finds less results than if we derive them by filtering incrementally.
				//If all works well, we should never find fewer results than Eclipse does.
				assertElementsAtLeast(results, JBOSS_RESULTS);
			}
		}
	}

	private void assertElementsAtLeast(List<String> results, String[] expecteds) {
		Set<String> actuals = ImmutableSet.copyOf(results);
		StringBuilder missing = new StringBuilder();
		boolean hasMissing = false;
		for (String e : expecteds) {
			if (!actuals.contains(e)) {
				missing.append(e+"\n");
				hasMissing = true;
			}
		}
		assertFalse("Missing elements:\n"+missing, hasMissing);
	}

	@SuppressWarnings("unused")
	private void assertElements(List<String> _actual, String... _expected) {
		String expected = toSortedString(Arrays.asList(_expected));
		String actual = toSortedString(_actual);
		assertEquals(expected, actual);
	}

	private String toSortedString(List<String> list) {
		ArrayList<String> sorted = new ArrayList<>(list);
		Collections.sort(sorted);
		StringBuilder buf = new StringBuilder();
		for (String string : sorted) {
			buf.append(string+"\n");
		}
		return buf.toString();
	}

	private List<String> getResults(CachingValueProvider p, String query) {
		return p.getValues(project, query).toStream()
		.map((h) -> h.getValue().toString())
		.collect(Collectors.toList());
	}

	private void dumpResults(String string, Collection<String> r) {
		System.out.println(">>> "+string);
		String[] strings = r.toArray(new String[r.size()]);
		Arrays.sort(strings);
		int i = 0;
		for (String s : strings) {
			System.out.println("\""+s+"\", //"+(++i));
//			System.out.println((++i)+" : "+s);
		}
		System.out.println("<<< "+string);
	}

}
