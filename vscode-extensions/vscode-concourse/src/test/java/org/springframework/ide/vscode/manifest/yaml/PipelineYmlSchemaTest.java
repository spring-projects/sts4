/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.ide.vscode.concourse.PipelineYmlSchema;

/**
 * @author Kris De Volder
 */
public class PipelineYmlSchemaTest {

//	@Test
//	public void shouldMakeSomeTests() {
//		fail("We should make some tests for this");
//	}
//	
//	private static final String[] NESTED_PROP_NAMES = {
////			"applications",
//			"buildpack",
//			"command",
//			"disk_quota",
//			"domain",
//			"domains",
//			"env",
//			"health-check-type",
//			"host",
//			"hosts",
////			"inherit",
//			"instances",
//			"memory",
//			"name",
//			"no-hostname",
//			"no-route",
//			"path",
//			"random-route",
//			"services",
//			"stack",
//			"timeout"
//	};
//
	private static final String[] TOPLEVEL_PROP_NAMES = {
			"resources",
			"jobs",
			"resource-types"
			//groups
	};

	PipelineYmlSchema schema = new PipelineYmlSchema();
//
//	@Test
//	public void toplevelProperties() throws Exception {
//		assertPropNames(schema.getTopLevelType().getProperties(DynamicSchemaContext.NULL), TOPLEVEL_PROP_NAMES);
//		assertPropNames(schema.getTopLevelType().getPropertiesMap(DynamicSchemaContext.NULL), TOPLEVEL_PROP_NAMES);
//	}
//
//	@Test
//	public void nestedProperties() throws Exception {
//		assertPropNames(getNestedProps(), NESTED_PROP_NAMES);
//	}
//
//	@Test
//	public void toplevelPropertiesHaveDescriptions() {
//		for (YTypedProperty p : schema.getTopLevelType().getProperties(DynamicSchemaContext.NULL)) {
//			if (!p.getName().equals("applications")) {
//				assertHasRealDescription(p);
//			}
//		}
//	}
//
//	@Test
//	public void nestedPropertiesHaveDescriptions() {
//		for (YTypedProperty p : getNestedProps()) {
//			assertHasRealDescription(p);
//		}
//	}
//
//	//////////////////////////////////////////////////////////////////////////////
//
//	private void assertHasRealDescription(YTypedProperty p) {
//		{
//			String noDescriptionText = Renderables.NO_DESCRIPTION.toHtml();
//			String actual = p.getDescription().toHtml();
//			String msg = "Description missing for '"+p.getName()+"'";
//			assertTrue(msg, StringUtil.hasText(actual));
//			assertFalse(msg, noDescriptionText.equals(actual));
//		}
//		{
//			String noDescriptionText = Renderables.NO_DESCRIPTION.toMarkdown();
//			String actual = p.getDescription().toMarkdown();
//			String msg = "Description missing for '"+p.getName()+"'";
//			assertTrue(msg, StringUtil.hasText(actual));
//			assertFalse(msg, noDescriptionText.equals(actual));
//		}
//	}
//
//	private List<YTypedProperty> getNestedProps() {
//		YSeqType applications = (YSeqType) schema.getTopLevelType().getPropertiesMap().get("applications").getType();
//		YBeanType application = (YBeanType) applications.getDomainType();
//		return application.getProperties();
//	}
//
//	private void assertPropNames(List<YTypedProperty> properties, String... expectedNames) {
//		assertEquals(ImmutableSet.copyOf(expectedNames), getNames(properties));
//	}
//
//	private void assertPropNames(Map<String, YTypedProperty> propertiesMap, String[] toplevelPropNames) {
//		assertEquals(ImmutableSet.copyOf(toplevelPropNames), ImmutableSet.copyOf(propertiesMap.keySet()));
//	}
//
//	private ImmutableSet<String> getNames(Iterable<YTypedProperty> properties) {
//		Builder<String> builder = ImmutableSet.builder();
//		for (YTypedProperty p : properties) {
//			builder.add(p.getName());
//		}
//		return builder.build();
//	}

}
