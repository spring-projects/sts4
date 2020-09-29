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
package org.springframework.ide.eclipse.boot.test.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.boot.test.AllSpringBootTests;

public class TestResourcesUtil {


	public static File getTestFile(String path) throws IOException {
		Bundle bundle = Platform.getBundle(AllSpringBootTests.PLUGIN_ID);
		File bundleFile = FileLocator.getBundleFile(bundle);
		Assert.assertNotNull(bundleFile);
		Assert.assertTrue("The bundle "+bundle.getBundleId()+" must be unpacked to allow using the embedded test resources", bundleFile.isDirectory());
		File file = new File(bundleFile, path);
		if (!file.exists()) {
			throw new FileNotFoundException("Test file : " + file.getAbsolutePath() + " not found");
		}
		return file;
	}

}
