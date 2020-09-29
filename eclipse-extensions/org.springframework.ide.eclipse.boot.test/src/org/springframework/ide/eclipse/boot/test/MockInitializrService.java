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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrDependencySpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;

import com.google.common.collect.ImmutableSet;

public class MockInitializrService implements InitializrService {

	private SpringBootStarters starters;
	private boolean unavailable = false;
	private boolean generateFakePom = false;
	private Set<String> supportedBootVersions;
	private MavenStartersHarness mavenHarness;
	private String initializrInfoInputFile;

	/**
	 *
	 * @param supportedBootVersions list of supported boot versions associated with
	 *                              the "valid" initializr URL. This is used by the
	 *                              wizard to check against the boot version of the
	 *                              local project. Used for testing error conditions
	 *                              (e.g. unsupported boot version errors that a
	 *                              wizard may throw)
	 * @param initializrInfoInput   a JSON test file that contains initializr info,
	 *                              like all available dependencies, that would
	 *                              otherwise be downloaded from a real initializr
	 *                              service.
	 */
	public MockInitializrService(String[] supportedBootVersions, MavenStartersHarness mavenHarness,
			String initializrInfoInputFile) {
		this.supportedBootVersions = ImmutableSet.copyOf(supportedBootVersions);
		this.mavenHarness = mavenHarness;
		this.initializrInfoInputFile = initializrInfoInputFile;
	}

	public void enableFakePomGenerator() {
		generateFakePom = true;
	}

	private InputStream getResource(String name, String endPoint) {
		return getClass().getResourceAsStream("edit-starters-test-inputs/" + name + "-" + endPoint + ".json");
	}

	@Override
	public SpringBootStarters getStarters(String bootVersion) throws Exception {
		// Mock unsupported boot version. This is an actual error thrown in the real
		// wizard
		if (!supportedBootVersions.contains(bootVersion)) {
			throw new FileNotFoundException();
		} else {
			if (unavailable) {
				throw new IOException("Initializr Service Unavailable");
			} else if (starters != null) {
				return starters;
			} else {
				return InitializrService.DEFAULT.getStarters(bootVersion);
			}
		}
	}

	/**
	 * Make the mock behave as if the 'dependencies' endpoint is not available
	 * (either the service is down, there is no internet connection, or this is an
	 * old service that doesn't implement the endpoint yet).
	 */
	public void makeUnavailable() {
		this.unavailable = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPom(Map<String, ?> parameters) throws Exception {
		if (unavailable) {
			throw new IOException("Initializr Service Unavailable");
		} else if (generateFakePom) {
			String bootVersion = (String) parameters.get("bootVersion");
			return mavenHarness.generateFakePom(getStarters(bootVersion),
					(List<String>) parameters.get("dependencies"));
		} else {
			return InitializrService.DEFAULT.getPom(parameters);
		}
	}

	public void loadInfo() throws Exception {
		setInputs(getResource(initializrInfoInputFile, "main"), getResource(initializrInfoInputFile, "dependencies"));
	}

	/**
	 * Causes the mock to parse input from given input streams instead of calling
	 * out to the real web service.
	 */
	private void setInputs(InputStream main, InputStream dependencies) throws Exception {
		starters = new SpringBootStarters(InitializrServiceSpec.parseFrom(main),
				InitializrDependencySpec.parseFrom(dependencies));
	}

}