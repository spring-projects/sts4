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

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract base class to aid in implementing a Dynamic model provider that executes a bosh
 * command (with `--json`) swtich and then extracts information from its json output.
 */
public abstract class BoshCommandBasedModelProvider<T> implements DynamicModelProvider<T> {

	private final YamlParser yamlParser;
	protected final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	protected Duration CMD_TIMEOUT = Duration.ofSeconds(10);

	protected BoshCommandBasedModelProvider() {
		Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		yamlParser = new YamlParser(new Yaml());
	}

	/**
	 * For deserializing the output from bosh cloud-config command.
	 */
	public static class BoshCommandResponse {
		private String[] blocks;

		@JsonProperty("Blocks")
		public String[] getBlocks() {
			return blocks;
		}
		public void setBlocks(String[] blocks) {
			this.blocks = blocks;
		}
	}

	protected String getBlock() throws Exception {
		String out = executeCommand(getCommand());
		BoshCommandResponse response = mapper.readValue(out, BoshCommandResponse.class);
		String[] blocks = response.getBlocks();
		Assert.isLegal(blocks!=null);
		Assert.isLegal(blocks.length==1);
		return blocks[0];
	}

	protected JsonNode getJsonTree() throws Exception {
		String out = executeCommand(getCommand());
		return mapper.readTree(out);
	}

	protected String executeCommand(ExternalCommand command) throws Exception {
		Log.info("executing cmd: "+command);
		ExternalProcess process = new ExternalProcess(getWorkingDir(), command, true, CMD_TIMEOUT);
		Log.info("executing cmd DONE: "+process);
		String out = process.getOut();
		return out;
	}

	protected File getWorkingDir() {
		return new File(".").getAbsoluteFile();
	}

	protected abstract ExternalCommand getCommand();

	protected YamlFileAST parseYaml(String block) throws Exception {
		TextDocument doc = new TextDocument(null, LanguageId.BOSH_CLOUD_CONFIG);
		doc.setText(block);
		YamlFileAST ast = yamlParser.getAST(doc);
		return ast;
	}
}
