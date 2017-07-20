/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh.models;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlTraversal;
import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Concrete implementation of {@link CloudConfigProvider} that runs `bosh cloud-config` command
 * with bosh cli and parses the output.
 *
 * @author Kris De Volder
 */
public class BoshCommandCloudConfigProvider implements DynamicModelProvider<CloudConfigModel> {

	ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	final YamlParser yamlParser;

	private Duration CMD_TIMEOUT = Duration.ofSeconds(10);

	public BoshCommandCloudConfigProvider() {
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		yamlParser = new YamlParser(new Yaml());
	}

	/**
	 * For deserializing the output from bosh cloud-config command.
	 */
	public static class CloudConfigResponse {
		private String[] blocks;

		@JsonProperty("Blocks")
		public String[] getBlocks() {
			return blocks;
		}

		public void setBlocks(String[] blocks) {
			this.blocks = blocks;
		}
	}

	YamlTraversal VM_TYPE_NAMES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("vm_types")
			.thenAnyChild()
			.thenValAt("name");

	protected static final YamlTraversal NETWORK_NAMES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("networks")
			.thenAnyChild()
			.thenValAt("name");

	protected static final YamlTraversal AVAILABILITY_ZONES = YamlPath.EMPTY
			.thenAnyChild()
			.thenValAt("azs")
			.thenAnyChild()
			.thenValAt("name");

	@Override
	public CloudConfigModel getModel(DynamicSchemaContext dc) throws Exception {
		String block = getCloudConfigBlock();
		TextDocument doc = new TextDocument(null, LanguageId.BOSH_CLOUD_CONFIG);
		doc.setText(block);
		YamlFileAST ast = yamlParser.getAST(doc);
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

	protected String getCloudConfigBlock() throws Exception{
		String out = executeBoshCloudConfigCommand();
		CloudConfigResponse response = mapper.readValue(out, CloudConfigResponse.class);
		String[] blocks = response.getBlocks();
		Assert.isLegal(blocks!=null);
		Assert.isLegal(blocks.length==1);
		return blocks[0];
	}

	/**
	 * Configure how long we wait for the command to fetch cloud config before
	 * raising timeout exception. (The command may block for long amounts of time
	 * of the director is unreachable on the network).
	 */
	public void setCommandTimeout(Duration duration) {
		this.CMD_TIMEOUT = duration;
	}

	protected String executeBoshCloudConfigCommand() throws Exception {
		ExternalCommand command = new ExternalCommand("bosh", "cloud-config", "--json");
		ExternalProcess process = new ExternalProcess(new File(".").getAbsoluteFile(), command, true, CMD_TIMEOUT);
		System.out.println("executeBoshCloudConfigCommand: "+process);
		String out = process.getOut();
		return out;
	}

}
