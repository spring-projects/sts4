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
package org.springframework.ide.vscode.bosh.models;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.ide.vscode.bosh.BoshCliConfig;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlTraversal;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

/**
 * Concrete implementation of {@link CloudConfigProvider} that runs `bosh cloud-config` command
 * with bosh cli and parses the output.
 *
 * @author Kris De Volder
 */
public class BoshCommandCloudConfigProvider extends BoshCommandBasedModelProvider<CloudConfigModel> {

	public BoshCommandCloudConfigProvider(BoshCliConfig config) {
		super(config);
	}

	private static final YamlTraversal VM_TYPE_NAMES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("vm_types")
			.thenAnyChild()
			.thenValAt("name");
	private static final YamlTraversal NETWORK_NAMES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("networks")
			.thenAnyChild()
			.thenValAt("name");
	private static final YamlTraversal AVAILABILITY_ZONES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("azs")
			.thenAnyChild()
			.thenValAt("name");
	private static final YamlTraversal DISK_TYPES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("disk_types")
			.thenAnyChild()
			.thenValAt("name");
	private static final YamlTraversal VM_EXTENSIONS = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("vm_extensions")
			.thenAnyChild()
			.thenValAt("name");

	@Override
	public CloudConfigModel getModel(DynamicSchemaContext dc) throws Exception {
		String block = getBlock();
		YamlFileAST ast = parseYaml(block);
		return new CloudConfigModel() {
			@Override
			public Collection<String> getVMTypes() {
				return getNames(VM_TYPE_NAMES);
			}

			@Override
			public Collection<String> getNetworkNames() {
				return getNames(NETWORK_NAMES);
			}

			@Override
			public Collection<String> getAvailabilityZones() {
				return getNames(AVAILABILITY_ZONES);
			}

			@Override
			public Collection<String> getDiskTypes() {
				return getNames(DISK_TYPES);
			}

			@Override
			public Collection<String> getVMExtensions() {
				return getNames(VM_EXTENSIONS);
			}

			private Collection<String> getNames(YamlTraversal namesPath) {
				return namesPath.traverseAmbiguously(ast)
				.flatMap(nameNode -> {
					String name = NodeUtil.asScalar(nameNode);
					return StringUtil.hasText(name)
							? Stream.of(name)
							: Stream.empty();
				})
				.collect(CollectorUtil.toMultiset());
			}
		};
	}

	@Override
	protected String[] getBoshCommand() {
		return new String[] {"cloud-config", "--json"};
	}

}
