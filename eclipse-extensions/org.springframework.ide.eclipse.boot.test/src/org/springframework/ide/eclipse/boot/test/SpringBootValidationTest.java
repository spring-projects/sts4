/*******************************************************************************
 * Copyright (c) 2015 Pivotal Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import org.springframework.ide.eclipse.boot.validation.framework.SpringBootProjectBuilder;

@SuppressWarnings("unchecked")
public class SpringBootValidationTest extends AbstractBootValidationTest {

	public static final String MARKER_ID = SpringBootProjectBuilder.MARKER_ID;

	@Test
	public void testScaffolding() throws Exception {
		String projectName = "simple-boot-project";
		createPredefinedProject(projectName);
		IJavaProject jp = getJavaProject(projectName);
		assertNotNull(jp.findType("demo.FooProperties"));
	}

	@Test
	public void testMissingConfigurationProcessorRuleOnClass() throws Exception {
		String projectName = "simple-boot-project";
		String resourcePath = "src/main/java/demo/FooProperties.java";

		IProject p = createPredefinedProject(projectName);

		IFile r = (IFile) p.findMember(resourcePath);
		IMarker[] markers = getAllMarkers(r, MARKER_ID);

		//Finds @ConfigrationProperties on Class?
		assertThat(markers, array(
				allOf(
						markerWithMessageSnippet("@ConfigurationProperties"),
						markerWithAreaCovering("ConfigurationProperties")
				)
		));
	}

	@Test
	public void testMissingConfigurationProcessorRuleOnMethod() throws Exception {
		String projectName = "simple-boot-project";
		String resourcePath = "src/main/java/demo/FooConfiguration.java";

		IProject p = createPredefinedProject(projectName);

		IFile r = (IFile) p.findMember(resourcePath);
		IMarker[] markers = getAllMarkers(r, MARKER_ID);

		//Finds @ConfigrationProperties on Class?
		assertThat(markers, array(
				allOf(
						markerWithMessageSnippet("@ConfigurationProperties"),
						markerWithAreaCovering("ConfigurationProperties")
				)
		));
	}

	@Test
	public void testMissingConfigurationProcessorQuickfix() throws Exception {
		String projectName = "simple-boot-project";
		String resourcePath = "src/main/java/demo/FooProperties.java";

		final IProject p = createPredefinedProject(projectName);

		final IFile r = (IFile) p.findMember(resourcePath);
		IMarker[] markers = getAllMarkers(r, MARKER_ID);

		//Finds @ConfigrationProperties on Class?
		assertThat(markers, array(
				allOf(
						markerWithMessageSnippet("@ConfigurationProperties"),
						markerWithAreaCovering("ConfigurationProperties")
				)
		));

		IMarker marker = markers[0];
		assertTrue(hasResolutions(marker));

		IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(marker);
		assertThat(resolutions, array(
				resolutionWithLabel("Add spring-boot-configuration-processor to pom.xml")
		));

		IFile pom = (IFile) p.findMember("pom.xml");
		assertThat(getContents(pom), not(containsString("spring-boot-configuration-processor")));

		IMarkerResolution quickfix = resolutions[0];
		quickfix.run(marker);
		assertThat(getContents(pom), containsString("<artifactId>spring-boot-configuration-processor</artifactId>"));
		new ACondition("marker disapears after quickfix") {
			public boolean test() throws Exception {
				buildProject(p);
				assertNoMarkers(getAllMarkers(r, MARKER_ID));
				return true;
			}
		}.waitFor(10000);
	}

	private void buildProject(IProject p) throws Exception {
		ISchedulingRule rule =ResourcesPlugin.getWorkspace().getRuleFactory().buildRule();
		Job.getJobManager().beginRule(rule, new NullProgressMonitor());
		try {
			p.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		} finally {
			Job.getJobManager().endRule(rule);
		}
	}

	private Matcher<IMarkerResolution> resolutionWithLabel(final String snippet) {
		return new TypeSafeMatcher<IMarkerResolution>() {

			public void describeTo(Description description) {
				description.appendText("resolutionWithLabel("+snippet+")");
			}

			protected void describeMismatchSafely(IMarkerResolution item,
					Description mismatchDescription) {
				String actual = item.getLabel();
				mismatchDescription.appendText("[expected ");
				mismatchDescription.appendDescriptionOf(this);
				mismatchDescription.appendText(" but label is: ");
				mismatchDescription.appendText(actual);
				mismatchDescription.appendText("]");
			}

			protected boolean matchesSafely(IMarkerResolution item) {
				String actual = item.getLabel();
				return actual.equals(snippet);
			}
		};
	}

	protected boolean hasResolutions(IMarker marker) {
		return IDE.getMarkerHelpRegistry().hasResolutions(marker);
	}

}
