package org.springframework.ide.vscode.boot.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
public class AdHocSpringPropertyIndexProvider implements SpringPropertyIndexProvider {

	private static final Logger log = LoggerFactory.getLogger(AdHocSpringPropertyIndexProvider.class);

	private static class SimplePropertyIndex extends FuzzyMap<PropertyInfo> {
		@Override
		protected String getKey(PropertyInfo entry) {
			return entry.getId();
		}
	}

	private Cache<IJavaProject, SimplePropertyIndex> indexes;
	final private JavaProjectFinder projectFinder;

	public AdHocSpringPropertyIndexProvider(JavaProjectFinder projectFinder, ProjectObserver projectObserver, FileObserver fileObserver) {
		this.projectFinder = projectFinder;
		this.indexes = CacheBuilder.newBuilder().build();
		if (projectObserver != null) {
			projectObserver.addListener(ProjectObserver.onAny(project -> indexes.invalidate(project)));
		}
		if (fileObserver!=null) {
			fileObserver.onAnyChange(ImmutableList.of(
					"**/application.properties",
					"**/application.yml"
			), changed -> {
				log.info("File changed: "+changed);
				projectFinder.find(new TextDocumentIdentifier(changed)).ifPresent(project -> {
					log.info("=> Project changed: "+project.getElementName());
					indexes.invalidate(project);
				});
			});
		}
	}


	@Override
	public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
		Optional<IJavaProject> jp = projectFinder.find(new TextDocumentIdentifier(doc.getUri()));
		if (jp.isPresent()) {
			return getIndex(jp.get());
		}
		return SpringPropertyIndex.EMPTY_INDEX;
	}


	private FuzzyMap<PropertyInfo> getIndex(IJavaProject jp) {
		try {
			return indexes.get(jp, () -> {
				SimplePropertyIndex index = new SimplePropertyIndex();
				IClasspathUtil.getSourceFolders(jp.getClasspath()).forEach(sourceFolder -> {
					processFile(this::parseProperties, new File(sourceFolder, "application.properties"), index);
					processFile(this::parseYaml, new File(sourceFolder, "application.yml"), index);
				});
				return index;
			});
		} catch (ExecutionException e) {
			log.error("", e);
		}
		return null;
	}

	private void processFile(Function<File, Properties> parserFunction, File file, SimplePropertyIndex index) {
		Properties props = parserFunction.apply(file);
		if (props!=null) {
			for (Object p : props.keySet()) {
				if (p instanceof String) {
					String filename = file.getName();
					index.add(new PropertyInfo((String)p).setDescription("Ad-hoc property defined in '"+filename+"'"));
				}
			}
		}
	}

	private Properties parseProperties(File propsFile) {
		try {
			if (propsFile.isFile()) {
				Properties props = new Properties();
				try (InputStream reader = new FileInputStream(propsFile)) {
					props.load(reader);
				}
				return props;
			}
		} catch (Exception e) {
			//ignore failed attempt to read bad file
		}
		return null;
	}

	private Properties parseYaml(File yamlFile) {
		if (yamlFile.isFile()) {
			Yaml yaml = new Yaml();
			try (Reader reader = new InputStreamReader(new FileInputStream(yamlFile), "UTF8")) {
				Properties props = new Properties();
				for (Node node : yaml.composeAll(reader)) {
					flattenProperties("", node, props);
				}
				return props;
			} catch (Exception e ) {
				//ignore failed attempt to read bad file
			}
		}
		return null;
	}


	private void flattenProperties(String prefix, Node node, Properties props) {
		switch (node.getNodeId()) {
		case mapping:
			if (!prefix.isEmpty()) {
				prefix = prefix +".";
			}
			MappingNode mapping = (MappingNode)node;
			for (NodeTuple tup : mapping.getValue()) {
				String key = NodeUtil.asScalar(tup.getKeyNode());
				if (key!=null) {
					flattenProperties(prefix+key, tup.getValueNode(), props);
				}
			}
			break;
		case scalar:
			//End of the line.
			props.put(prefix, NodeUtil.asScalar(node));
			break;
		default:
			//Ignore other cases, might implement later if it makes sense.
			break;
		}
	}

}
