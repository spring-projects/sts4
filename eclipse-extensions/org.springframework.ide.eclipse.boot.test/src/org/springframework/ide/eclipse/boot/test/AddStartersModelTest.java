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
import static org.junit.Assert.assertTrue;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.latestBootReleaseVersion;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersCompareModel;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersCompareResult;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersError;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersInitializrService;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersPreferences;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizardModel;
import org.springframework.ide.eclipse.boot.wizard.starters.InitializrModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class AddStartersModelTest extends AddStartersTestHarness {

	private static boolean wasAutobuilding;

	@BeforeClass
	public static void setupClass() throws Exception {
		wasAutobuilding = StsTestUtil.isAutoBuilding();
		StsTestUtil.setAutoBuilding(false);
	}

	@AfterClass
	public static void teardownClass() throws Exception {
		StsTestUtil.setAutoBuilding(wasAutobuilding);
	}

	@Before
	public void setup() throws Exception {
		StsTestUtil.cleanUpProjects();
	}

	@Rule
	public TestBracketter testBracketer = new TestBracketter();

	/**
	 * Tests that the initializr model with dependencies is loaded in the wizard
	 */
	@Test
	public void selectDependenciesInWizard() throws Exception {
		IProject project = harness.createBootProject("selectDependenciesInWizard", latestBootReleaseVersion());

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] supportedBootVersions = harness.getInitializrSupportedBootVersions();
		String[] dependenciesToSelect = new String[] {"web", "actuator"};

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);

		String projectBootVersion = getProjectBootVersion(initializrService, validInitializrUrl, project);

		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		// Verify the fields and model are set in the wizard after loading
		assertEquals(validInitializrUrl, wizard.getServiceUrl().getValue());
		assertEquals(projectBootVersion, wizard.getBootVersion().getValue());
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);

		// Ensure dependency info are loaded in the initializr Model
		// and dependencies are selected
		InitializrModel initializrModel = wizard.getInitializrModel().getValue();
		HierarchicalMultiSelectionFieldModel<Dependency> dependencies = initializrModel.dependencies;
		List<CheckBoxModel<Dependency>> allDependencies = dependencies.getAllBoxes();
		assertTrue(!allDependencies.isEmpty());

		// Check that the actual selected dependencies in the wizard match the expected dependencies
		List<Dependency> currentSelection = dependencies.getCurrentSelection();
		assertTrue(currentSelection.size() > 0);
		assertEquals(dependenciesToSelect.length, currentSelection.size());
		for (Dependency selected : currentSelection) {
			assertNotNull(getExpectedDependency(selected, dependenciesToSelect));
		}

		// There should be no comparison, as we haven't downloaded the project to compare to in this test
		AddStartersCompareModel compareModel = wizard.getCompareModel().getValue();
		assertNull(compareModel.getComparison().getValue());
	}


	@Test
	public void changeBetweenInvalidAndValidUrl() throws Exception {
		IProject project = harness.createBootProject("changeBetweenInvalidAndValidUrl", latestBootReleaseVersion());
		String[] supportedBootVersions = harness.getInitializrSupportedBootVersions();

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] dependenciesToSelect = new String[] {"web", "actuator"};

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);

		String projectBootVersion = getProjectBootVersion(initializrService, validInitializrUrl, project);

		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		// Verify the fields and model are set in the wizard after loading
		assertEquals(validInitializrUrl, wizard.getServiceUrl().getValue());
		assertEquals(projectBootVersion, wizard.getBootVersion().getValue());
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);

		// Set a valid URL that is not a valid initializr URL
		wizard.getServiceUrl().setValue("https://www.google.ca");
		waitForWizardJob();
		// There should be no valid initializr model available
		assertInitializrAndCompareModelsNull(wizard);
		AddStartersError result = (AddStartersError)wizard.getValidator().getValue();
		assertTrue(result.status == IStatus.ERROR);
		assertTrue(result.details.contains("ConnectException"));

		// Set a valid URL again. Should load a valid model and validate
		wizard.getServiceUrl().setValue(validInitializrUrl);
		waitForWizardJob();
		assertInitializrAndCompareModelsNotNull(wizard);
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
	}


	@Test
	public void malformedUrlError() throws Exception {
		IProject project = harness.createBootProject("malformedUrlError", latestBootReleaseVersion());
		String[] supportedBootVersions = harness.getInitializrSupportedBootVersions();

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] dependenciesToSelect = new String[] {"web", "actuator"};

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);

		String projectBootVersion = getProjectBootVersion(initializrService, validInitializrUrl, project);

		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		// Verify the fields and model are set in the wizard after loading
		assertEquals(validInitializrUrl, wizard.getServiceUrl().getValue());
		assertEquals(projectBootVersion, wizard.getBootVersion().getValue());
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);

		// Set a malformed URL
		wizard.getServiceUrl().setValue("wlwlwlw");
		waitForWizardJob();

		// There should be no valid initializr model available
		assertInitializrAndCompareModelsNull(wizard);
		AddStartersError result = (AddStartersError)wizard.getValidator().getValue();
		assertTrue(result.status == IStatus.ERROR);
		assertTrue(result.details.contains("MalformedURLException"));

		// Set a valid URL again. Should load a valid model and validate
		wizard.getServiceUrl().setValue(validInitializrUrl);
		waitForWizardJob();
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);
	}


	@Test
	public void missingUrlError() throws Exception {
		IProject project = harness.createBootProject("missingUrlError", latestBootReleaseVersion());
		String[] supportedBootVersions = harness.getInitializrSupportedBootVersions();

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] dependenciesToSelect = new String[] {"web", "actuator"};

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);

		String projectBootVersion = getProjectBootVersion(initializrService, validInitializrUrl, project);

		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		// Verify the fields and model are set in the wizard after loading
		assertEquals(validInitializrUrl, wizard.getServiceUrl().getValue());
		assertEquals(projectBootVersion, wizard.getBootVersion().getValue());
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);

		// Set empty URL
		wizard.getServiceUrl().setValue("");
		waitForWizardJob();

		// There should be no valid initializr model available
		AddStartersError result = (AddStartersError)wizard.getValidator().getValue();
		assertTrue(result.status == IStatus.ERROR);
		assertTrue(result.details.contains("Missing initializr service URL"));
		assertInitializrAndCompareModelsNull(wizard);

		// Set a valid URL again. Should load a valid model and validate
		wizard.getServiceUrl().setValue(validInitializrUrl);
		waitForWizardJob();
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());
		assertInitializrAndCompareModelsNotNull(wizard);
	}

	@Test
	public void unsupportedBootVersionError() throws Exception {

		// Create a project with a valid boot version as the harness
		// doesn't allow creating a project with old unsupported boot version
		// However we will change the list of supported boot versions to exclude this boot version
		// to simulate a case where there is a unsupported boot version in the add starters wizard
		IProject project = harness.createBootProject("unsupportedBootVersionError", latestBootReleaseVersion());

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;

		// List supported versions that do not include the version used to create the project
		String[] supportedBootVersions = new String[] { "1.0.0.RELEASE", "1.1.0.RELEASE", "1.5.3.RELEASE"};

		// No dependencies to select, as in this test, there should be no initializr information because boot version is not supported
		String[] dependenciesToSelect = null;

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);
		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		AddStartersError result = (AddStartersError)wizard.getValidator().getValue();
		assertTrue(result.status == IStatus.ERROR);
		assertTrue(result.details.contains("FileNotFoundException"));

		// Also verify that the error message details lists all the supported boot versions that a user should update to
		assertTrue(result.details.contains("1.0.0.RELEASE"));
		assertTrue(result.details.contains("1.1.0.RELEASE"));
		assertTrue(result.details.contains("1.5.3.RELEASE"));
	}

	@Test
	public void basicComparison() throws Exception {
		IProject project = harness.createBootProject("basicComparison", latestBootReleaseVersion());
		String[] supportedBootVersions = harness.getInitializrSupportedBootVersions();

		String starterZipFile = STARTER_ZIP_WEB_ACTUATOR;
		String validInitializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] dependenciesToSelect = new String[] {"web", "actuator"};

		MockInitializrService wrappedService = new MockInitializrService(supportedBootVersions, mavenStartersHarness,
				INITIALIZR_INFO_INPUT);
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions, dependenciesToSelect, wrappedService);
		AddStartersPreferences preferences = createPreferences(validInitializrUrl, prefs);
		AddStartersWizardModel wizard = createAndLoadWizard(project, preferences, initializrService,
				dependenciesToSelect);

		assertInitializrAndCompareModelsNotNull(wizard);

		AddStartersCompareModel compareModel = wizard.getCompareModel().getValue();
		AddStartersCompareResult comparison = compareModel.getComparison().getValue();
		assertNull(comparison);

		compareModel.generateComparison(new NullProgressMonitor());
		comparison = compareModel.getComparison().getValue();
		assertNotNull(comparison);
		// Verify that the comparison contains the "mocked" downloaded zip file as a comparison source
		assertTrue(comparison.getDownloadedProject().getPath().contains(starterZipFile));
		// Verify that the comparison contains the local project as a comparison source
		assertTrue(comparison.getLocalResource().getProject().equals(project));
	}
}
