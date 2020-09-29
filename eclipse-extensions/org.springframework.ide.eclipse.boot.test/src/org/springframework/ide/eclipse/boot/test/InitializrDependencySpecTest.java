/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrDependencySpec;

/**
 * @author Kris De Volder
 */
public class InitializrDependencySpecTest {

	/**
	 * Basic test, parses data from initialzr dependencies endpoint
	 */
	@Test
	public void parseInitializrDependencies() throws Exception {
		InitializrDependencySpec spec = parseDependencies("edit-starters-test-inputs/sample-dependencies.json");
		assertNotNull(spec);
	}

	private InitializrDependencySpec parseDependencies(String resourceName) throws Exception {
		InputStream rsrc = this.getClass().getResourceAsStream(resourceName);
		try {
			return InitializrDependencySpec.parseFrom(rsrc);
		} finally {
			rsrc.close();
		}
	}

	//TODO: EditStartersModel testing
	//  - most popular section doesn't add duplicate checkboxes
	//  - most popular section always adds all the initially selected checkboxes
	//  - dependencies already selected in pom are correctly reflected as initial selection
	//  - popularity tracker only updates count for newly selected dependencies, not those that where there before
	//  - edit starters performApply modifies the pom correctly to a
	//      - add dependencies
	//          with scope
	//          with version
	//      - add boms
	//      - add repository

}
