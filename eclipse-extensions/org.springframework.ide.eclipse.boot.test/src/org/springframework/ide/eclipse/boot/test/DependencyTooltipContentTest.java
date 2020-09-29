/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
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

import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.DependencyTooltipContent;

import org.junit.Test;

public class DependencyTooltipContentTest {

	@Test public void generateRequirementTooltip_common_cases() throws Exception {
		assertTooltip("Requires Spring Boot >=1.0.0", "1.0.0");
		assertTooltip("Requires Spring Boot >=1.0.0 and <=2.0.0", "[1.0.0,2.0.0]");
		assertTooltip("Requires Spring Boot >=1.0.0 and <2.0.0", "[1.0.0,2.0.0)");
		assertTooltip("Requires Spring Boot >1.0.0 and <2.0.0", "(1.0.0,2.0.0)");
	}

	@Test public void generateRequirementTooltip_edge_cases() throws Exception {
		assertTooltip(null, null);
		assertTooltip(null, "");
		assertEquals(null, DependencyTooltipContent.generateRequirements(null));
	}

	private void assertTooltip(String expectedTooltip, String versionRange) {
		Dependency dep = new Dependency();
		dep.setVersionRange(versionRange);
		assertEquals(expectedTooltip, DependencyTooltipContent.generateRequirements(dep));
	}

}
