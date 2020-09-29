/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test rule that enables or disables autobuilding before test exeuction and
 * restores this setting back to its original state at end of test.
 *
 * @author Kris De Volder
 */
public class AutobuildingEnablement implements TestRule {

	private boolean enable;

	public AutobuildingEnablement(boolean enable) {
		this.enable =  enable;
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			public void evaluate() throws Throwable {
				boolean wasAutoBuilding = StsTestUtil.isAutoBuilding();
				StsTestUtil.setAutoBuilding(enable);
				try {
					base.evaluate();
				} finally {
					StsTestUtil.setAutoBuilding(wasAutoBuilding);
				}
			}
		};
	}

}
