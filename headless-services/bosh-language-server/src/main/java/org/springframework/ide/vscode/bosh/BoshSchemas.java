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
package org.springframework.ide.vscode.bosh;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ide.vscode.bosh.models.BoshModels;
import org.springframework.ide.vscode.commons.util.Lazy;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class BoshSchemas implements YamlSchema {

	public final YTypeFactory f = new YTypeFactory()
			.enableTieredProposals(false)
			.suggestDeprecatedProperties(false);
	public BoshDeploymentManifestSchema getDeploymentSchema() {
		return deploymentSchema;
	}

	private final Lazy<Collection<YType>> defTypes = new Lazy<>();
	private final Lazy<Collection<Pair<YType,YType>>> defAndRefTypes = new Lazy<>();

	private final BoshDeploymentManifestSchema deploymentSchema;

	private final YType toplevelType;
	private BoshCloudConfigSchema cloudConfigSchema;


	public BoshSchemas(BoshModels models) {
		this.deploymentSchema = new BoshDeploymentManifestSchema(f, models);
		this.cloudConfigSchema = new BoshCloudConfigSchema(f, models);
		toplevelType = f.contextAware("BoshSchemas", dc -> {
			return LanguageId.BOSH_CLOUD_CONFIG.equals(dc.getDocument().getLanguageId())
				? cloudConfigSchema.getTopLevelType()
				: deploymentSchema.getTopLevelType();
		});
	}

	@Override
	public YType getTopLevelType() {
		return toplevelType;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return f.TYPE_UTIL;
	}

	public Collection<Pair<YType,YType>> getDefAndRefTypes() {
		return defAndRefTypes.load(() -> {
			Builder<Pair<YType, YType>> builder = ImmutableList.builder();
			builder.addAll(cloudConfigSchema.getDefAndRefTypes());
			builder.addAll(deploymentSchema.getDefAndRefTypes());
			return builder.build();
		});
	}

	public Collection<YType> getDefinitionTypes() {
		return defTypes.load(() -> {
			Builder<YType> builder = ImmutableList.builder();
			builder.addAll(cloudConfigSchema.getDefinitionTypes());
			builder.addAll(deploymentSchema.getDefinitionTypes());
			return builder.build();
		});
	}

}
