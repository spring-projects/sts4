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
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrl;
import org.springframework.ide.eclipse.boot.test.util.TestResourcesUtil;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersCompareModel;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersInitializrService;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersPreferences;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizardModel;
import org.springframework.ide.eclipse.boot.wizard.starters.InitializrModel;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

public class AddStartersTestHarness {

	protected BootProjectTestHarness harness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	protected MavenStartersHarness mavenStartersHarness = new MavenStartersHarness();
	protected IPreferenceStore prefs = new MockPrefsStore();


	protected static final String MOCK_VALID_INITIALIZR_URL = "https://add.starters.start.spring.io";

	// A starter zip file containing "selected" web and actuator dependencies
	protected static final String STARTER_ZIP_WEB_ACTUATOR = "/initializr/boot-web-actuator/starter.zip";
	protected static final String INITIALIZR_INFO_INPUT = "sample";


	protected void loadInitializrModel(AddStartersWizardModel wizard) throws Exception {
		wizard.addModelLoader(() -> wizard.createInitializrModel(new NullProgressMonitor()));
		// Wait for it to finish
		waitForWizardJob();
	}

	protected ISpringBootProject getBootProject(AddStartersInitializrService service, String url, IProject project) throws Exception {
		InitializrService initializr = service.getService(() -> url);
		SpringBootCore core = new SpringBootCore(initializr);
		return core.project(project);
	}

	protected String getProjectBootVersion(AddStartersInitializrService service, String url, IProject project) throws Exception {
		ISpringBootProject bootProject = getBootProject(service, url, project);
		return bootProject.getBootVersion();
	}

	protected void waitForWizardJob() throws InterruptedException {
		Job.getJobManager().join(AddStartersWizardModel.JOB_FAMILY, null);
	}

	protected void assertInitializrAndCompareModelsNull(AddStartersWizardModel wizard) {
		InitializrModel initializrModel = wizard.getInitializrModel().getValue();
		assertNull(initializrModel);
		AddStartersCompareModel compareModel = wizard.getCompareModel().getValue();
		assertNull(compareModel);
	}

	protected void assertInitializrAndCompareModelsNotNull(AddStartersWizardModel wizard) {
		InitializrModel initializrModel = wizard.getInitializrModel().getValue();
		assertNotNull(initializrModel);
		AddStartersCompareModel compareModel = wizard.getCompareModel().getValue();
		assertNotNull(compareModel);
	}

	protected void selectDependenciesInWizard(AddStartersWizardModel wizard, String... dependencies) {
		InitializrModel initializrModel = wizard.getInitializrModel().getValue();
		for (String dep : dependencies) {
			initializrModel.addDependency(dep);
		}
	}


	protected AddStartersWizardModel createAndLoadWizard(IProject project, AddStartersPreferences preferences,
			AddStartersInitializrService initializrService, String[] dependenciesToSelect) throws Exception {

		AddStartersWizardModel wizard = new AddStartersWizardModel(project, preferences, initializrService);

		// Wizard model is not available until it is loaded
		assertInitializrAndCompareModelsNull(wizard);

		loadInitializrModel(wizard);

		// Select the dependencies in the "Dependencies" section of the wizard
		if (dependenciesToSelect != null) {
			selectDependenciesInWizard(wizard, dependenciesToSelect);
		}

		return wizard;
	}

	protected AddStartersPreferences createPreferences(String validInitializrUrl, IPreferenceStore prefs) {
		return  new MockAddStartersPreferences(validInitializrUrl, prefs);
	}

	protected void performOk(AddStartersWizardModel wizard) throws Exception {
		wizard.performOk();
		waitForWizardJob();
	}

	public static class MockAddStartersPreferences extends AddStartersPreferences {

		private final List<String> storedUrls = new ArrayList<>();

		public MockAddStartersPreferences(String validUrl, IPreferenceStore prefs) {
			super(prefs);
			storedUrls.add(validUrl);
		}

		@Override
		public String getInitializrUrl() {
			return storedUrls.get(0);
		}

		@Override
		public String[] getInitializrUrls() {
			return storedUrls.toArray(new String[] {});
		}

		@Override
		public void addInitializrUrl(String url) {
			storedUrls.add(url);
		}

	}

	public static class MockProjectDownloader extends InitializrProjectDownloader {

		private final String starterZipPath;
		private final String[] expectedSelectedDependencies;

		public MockProjectDownloader(URLConnectionFactory urlConnectionFactory, InitializrUrl url,
				String starterZipPath, String[] expectedSelectedDependencies) {
			super(urlConnectionFactory, url);
			this.starterZipPath = starterZipPath;
			this.expectedSelectedDependencies = expectedSelectedDependencies;
		}

		@Override
		public File getProject(List<Dependency> dependencies, ISpringBootProject bootProject) throws Exception {
			// selected dependencies from the wizard don't actually get used in the mock
			// project downloader
			// as we dont actually need to download the zip file from initializr using a
			// constructed URL that
			// contains the dependencies.
			// However, we can at least test that the dependencies that were selected in the
			// wizard match the expected ones
			assertSelectedDependencies(dependencies);

			return TestResourcesUtil.getTestFile(starterZipPath);
		}

		private void assertSelectedDependencies(List<Dependency> dependencies) {
			assertEquals(expectedSelectedDependencies.length, dependencies.size());
			for (Dependency actual : dependencies) {
				assertNotNull(getExpectedDependency(actual, expectedSelectedDependencies));
			}
		}
	}

	public static String getExpectedDependency(Dependency dep, String... expectedDependencies) {
		for (String expected : expectedDependencies) {
			if (expected.equals(dep.getId())) {
				return expected;
			}
		}
		return null;
	}

}
