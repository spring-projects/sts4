/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YSeqType;
import org.springframework.ide.vscode.manifest.yaml.ManifestYmlSchema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * @author Kris De Volder
 */
public class ManifestYmlSchemaTest {

	private static final String[] NESTED_PROP_NAMES = {
//			"applications",
			"buildpack",
			"buildpacks",
			"command",
			"disk_quota",
			"docker",
			"domain",
			"domains",
			"env",
			"health-check-http-endpoint",
			"health-check-type",
			"host",
			"hosts",
//			"inherit",
			"instances",
			"memory",
			"name",
			"no-hostname",
			"no-route",
			"path",
			"random-route",
			"routes",
			"services",
			"stack",
			"timeout"
	};

	private static final String[] TOPLEVEL_PROP_NAMES = {
			"applications",
			"buildpack",
			"buildpacks",
			"command",
			"disk_quota",
			"domain",
			"domains",
			"env",
			"health-check-http-endpoint",
			"health-check-type",
//			"host",
//			"hosts",
			"inherit",
			"instances",
			"memory",
//			"name",
			"no-hostname",
			"no-route",
			"path",
			"random-route",
//			"routes",
			"services",
			"stack",
			"timeout"
	};

	ManifestYmlSchema schema = new ManifestYmlSchema(EMPTY_PROVIDERS);

	@Test
	public void toplevelProperties() throws Exception {
		assertPropNames(schema.getTopLevelType().getProperties(), TOPLEVEL_PROP_NAMES);
		assertPropNames(schema.getTopLevelType().getPropertiesMap(), TOPLEVEL_PROP_NAMES);
	}

	@Test
	public void nestedProperties() throws Exception {
		assertPropNames(getNestedProps(), NESTED_PROP_NAMES);
	}

	@Test
	public void toplevelPropertiesHaveDescriptions() {
		for (YTypedProperty p : schema.getTopLevelType().getProperties()) {
			if (!p.getName().equals("applications")) {
				assertHasRealDescription(p);
			}
		}
	}

	@Test
	public void nestedPropertiesHaveDescriptions() {
		for (YTypedProperty p : getNestedProps()) {
			assertHasRealDescription(p);
		}
	}

	//////////////////////////////////////////////////////////////////////////////

	private void assertHasRealDescription(YTypedProperty p) {
		{
			String noDescriptionText = Renderables.NO_DESCRIPTION.toHtml();
			String actual = p.getDescription().toHtml();
			String msg = "Description missing for '"+p.getName()+"'";
			assertTrue(msg, StringUtil.hasText(actual));
			assertFalse(msg, noDescriptionText.equals(actual));
		}
		{
			String noDescriptionText = Renderables.NO_DESCRIPTION.toMarkdown();
			String actual = p.getDescription().toMarkdown();
			String msg = "Description missing for '"+p.getName()+"'";
			assertTrue(msg, StringUtil.hasText(actual));
			assertFalse(msg, noDescriptionText.equals(actual));
		}
	}

	private List<YTypedProperty> getNestedProps() {
		YSeqType applications = (YSeqType) schema.getTopLevelType().getPropertiesMap().get("applications").getType();
		AbstractType application = (AbstractType) applications.getDomainType();
		return application.getProperties();
	}

	private void assertPropNames(List<YTypedProperty> properties, String... expectedNames) {
		assertEquals(ImmutableSet.copyOf(expectedNames), getNames(properties));
	}

	private void assertPropNames(Map<String, YTypedProperty> propertiesMap, String[] toplevelPropNames) {
		assertEquals(ImmutableSet.copyOf(toplevelPropNames), ImmutableSet.copyOf(propertiesMap.keySet()));
	}

	private ImmutableSet<String> getNames(Iterable<YTypedProperty> properties) {
		Builder<String> builder = ImmutableSet.builder();
		for (YTypedProperty p : properties) {
			builder.add(p.getName());
		}
		return builder.build();
	}

	private static final ManifestYmlHintProviders EMPTY_PROVIDERS = new ManifestYmlHintProviders() {

		@Override
		public Callable<Collection<YValueHint>> getServicesProvider() {
			return null;
		}

		@Override
		public Callable<Collection<YValueHint>> getDomainsProvider() {
			return null;
		}

		@Override
		public Callable<Collection<YValueHint>> getBuildpackProviders() {
			return null;
		}

		@Override
		public Callable<Collection<YValueHint>> getStacksProvider() {
			return null;
		}
	};
}
