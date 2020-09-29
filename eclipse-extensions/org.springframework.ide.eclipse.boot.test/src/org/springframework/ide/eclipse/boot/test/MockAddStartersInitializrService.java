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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Supplier;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrl;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Option;
import org.springframework.ide.eclipse.boot.test.AddStartersTestHarness.MockProjectDownloader;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersInitializrService;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class MockAddStartersInitializrService extends AddStartersInitializrService {

	private final String starterZipPath;

	private final String validInitializrUrl;

	private final String[] expectedSelectedDependencies;

	private final MockInitializrService wrappedService;

	private final String[] supportedBootVersions;

	// Empty URL factory. Shouldn't do anything as an actual URL connection is not
	// needed for testing
	private static final URLConnectionFactory EMPTY_URL_FACTORY = new URLConnectionFactory() {

		@Override
		public URLConnection createConnection(URL url) throws IOException {
			return null;
		}

	};

	/**
	 *
	 *
	 * @param starterZipPath               path to an starter zip file representing
	 *                                     a project that would otherwise be
	 *                                     downloaded from an actual initializr
	 *                                     `/starter.zip` endpoint
	 * @param validInitializrUrl           an initializr service URL that is
	 *                                     considered "valid" for the wizard (does
	 *                                     not have to be a real-life initializr URL
	 *                                     as no connection will be attempted), and
	 *                                     read by the wizard when it initially is
	 *                                     created. Mocks having a valid URL like
	 *                                     "https://start.spring.io " in an actual
	 *                                     boot initializr preference that is
	 *                                     initially read by the real wizard when it
	 *                                     opens.
	 * @param supportedBootVersions        list of supported boot versions
	 *                                     associated with the "valid" initializr
	 *                                     URL. This is used by the wizard to check
	 *                                     against the boot version of the local
	 *                                     project. Used for testing error
	 *                                     conditions (e.g. unsupported boot version
	 *                                     errors that a wizard may throw)
	 * @param expectedSelectedDependencies List of selected dependencies that are
	 *                                     expected when a request is made to
	 *                                     download a project within the wizard
	 *                                     mechanics
	 */
	public MockAddStartersInitializrService(String starterZipPath, String validInitializrUrl,
			String[] supportedBootVersions, String[] expectedSelectedDependencies,
			MockInitializrService wrappedService) {
		super(EMPTY_URL_FACTORY);
		this.starterZipPath = starterZipPath;
		this.validInitializrUrl = validInitializrUrl;
		this.expectedSelectedDependencies = expectedSelectedDependencies != null ? expectedSelectedDependencies
				: new String[0];

		this.wrappedService = wrappedService;
		this.supportedBootVersions = supportedBootVersions;
	}

	@Override
	public InitializrService getService(Supplier<String> url) {

		try {
			wrappedService.loadInfo();
		} catch (Exception e) {
			// print the error so its visible during tests
			e.printStackTrace();
			return null;
		}
		return wrappedService;
	}

	@Override
	public InitializrProjectDownloader getProjectDownloader(InitializrUrl url) {
		return new MockProjectDownloader(urlConnectionFactory, url, starterZipPath, expectedSelectedDependencies);
	}

	@Override
	protected Option[] resolveAllBootVersions(String url) throws Exception {
		Builder<Object> options = ImmutableList.builder();
		for (String v : supportedBootVersions) {
			Option option = new Option();
			option.setId(v);
			options.add(option);
		}
		return options.build().toArray(new Option[0]);
	}

	@Override
	public void checkBasicConnection(URL url) throws Exception {
		// Tests an actual error thrown by initializr service: a valid URL (e.g.
		// https://www.google.com) that is
		// not an initializr URL
		if (!validInitializrUrl.equals(url.toString())) {
			throw new ConnectException();
		}
	}

}
