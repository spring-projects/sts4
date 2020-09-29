/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog;
import org.springframework.ide.eclipse.boot.dash.test.actuator.ActuatorDataTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.AppNameReconcilerTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.CFRouteTests;
import org.springframework.ide.eclipse.boot.dash.test.yaml.DeploymentProperties2YamlTest;
import org.springframework.ide.eclipse.boot.dash.test.yaml.ManifestCompareMergeTests;
import org.springframework.ide.eclipse.boot.dash.test.yaml.Yaml2DeploymentPropertiesTest;

@RunWith(Suite.class)
@SuiteClasses({
	//Tests suites are put in order roughly based on
	// how long it takes to run them. Faster ones at the top.


	// Raised up temporarily to be run first.
	BootDashDockerTests.class, //50 seconds
	SelectDockerDaemonDialogTest.class,

	//New: (move down the chain later based on runtime)
	JmxSupportTest.class,
	PropertyFileStoreTest.class,

	// Manifest YAML/Deployment Properties tests (less than 2 seconds per suite)
	DeploymentProperties2YamlTest.class,
	Yaml2DeploymentPropertiesTest.class,
	AppNameReconcilerTest.class,
	RouteBuilderTest.class,
	CFRouteTests.class,

	//Really short (less than 2 seconds per suite):
	JLRMethodParserTest.class,
	OrderBasedComparatorTest.class,
	ManifestCompareMergeTests.class,
	AbstractLaunchConfigurationsDashElementTest.class,
	BootDashElementTagsTests.class,
	ActuatorDataTest.class,
	ToggleFiltersModelTest.class,

	//Medium length (less than 30 seconds):
	JarNameGeneratorTest.class,
	BootDashViewModelTest.class,
	BootJarPackagingTest.class,
	BeanResourceDefinitionParsingTests.class,

	//Long tests (more than 30 seconds):
	CloudFoundryBootDashModelMockingTest.class,
	DeploymentPropertiesDialogModelTests.class,
	BootDashActionTests.class,
	BootDashModelTest.class,
	CloudFoundryClientTest.class,
	CloudFoundryBootDashModelIntegrationTest.class,
})
public class AllBootDashTests {

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash.test";

}
