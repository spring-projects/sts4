/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;

/**
 * @author Martin Lippert
 */
public class LiveBeansModelTest {

	@Test
	public void testSimpleModel() throws Exception {
		String json = IOUtils.toString(getResourceAsStream("/live-beans-models/simple-live-beans-model.json"));
		LiveBeansModel model = LiveBeansModel.parse(json);

		LiveBean[] bean = model.getBeansOfType("org.test.DependencyA").toArray(new LiveBean[0]);
		assertEquals(1, bean.length);
		assertEquals("dependencyA", bean[0].getId());
		assertEquals("singleton", bean[0].getScope());
		assertEquals("org.test.DependencyA", bean[0].getType());
		assertEquals("file [/test-projects/classes/org/test/DependencyA.class]", bean[0].getResource());
		assertEquals(0, bean[0].getAliases().length);
		assertEquals(0, bean[0].getDependencies().length);

		bean = model.getBeansOfName("dependencyB").toArray(new LiveBean[0]);
		assertEquals(1, bean.length);
		assertEquals("dependencyB", bean[0].getId());
		assertEquals("singleton", bean[0].getScope());
		assertEquals("org.test.DependencyB", bean[0].getType());
		assertEquals("file [/test-projects/classes/org/test/DependencyB.class]", bean[0].getResource());
		assertEquals(0, bean[0].getAliases().length);
		assertEquals(0, bean[0].getDependencies().length);
	}

	@Test
	public void testEmptyModel() throws Exception {
		String json = IOUtils.toString(getResourceAsStream("/live-beans-models/empty-live-beans-model.json"));
		LiveBeansModel model = LiveBeansModel.parse(json);

		List<LiveBean> bean = model.getBeansOfType("org.test.DependencyA");
		assertEquals(0, bean.size());
	}

	@Test
	public void testTotallyEmptyModel() throws Exception {
		String json = IOUtils.toString(getResourceAsStream("/live-beans-models/totally-empty-live-beans-model.json"));
		LiveBeansModel model = LiveBeansModel.parse(json);

		List<LiveBean> bean = model.getBeansOfType("org.test.DependencyA");
		assertEquals(0, bean.size());
	}

	@Test
	public void custom_object_mapper_NON_DEFAULT_inclusion() throws IOException {
		//See https://github.com/spring-projects/sts4/issues/80
		String json = IOUtils.toString(getResourceAsStream("/live-beans-models/custom_object_mapper_NON_DEFAULT_inclusion.json"));
		LiveBeansModel model = LiveBeansModel.parse(json);
		List<LiveBean> beans = model.getBeansOfType("org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter");
		assertFalse(beans.isEmpty());
		assertEquals(2, beans.get(0).getDependencies().length);
	}

	private InputStream getResourceAsStream(String string) {
		return LiveBeansModelTest.class.getResourceAsStream(string);
	}

}
