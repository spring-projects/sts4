/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.maven;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintLabelPart;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.validation.generations.GenerationsValidator;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidationUtils;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.Version;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.InlayHintHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class PomInlayHintHandler implements InlayHintHandler {

	private static final String POM_XML = "pom.xml";

	private static final Logger log = LoggerFactory.getLogger(PomInlayHintHandler.class);
	
	final private SimpleLanguageServer server;
	final private JavaProjectFinder projectFinder;
	final private SpringProjectsProvider generationsProvider;
	
	public PomInlayHintHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder, ProjectObserver projectObserver, SpringProjectsProvider generationsProvider) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.generationsProvider = generationsProvider;
		
		projectObserver.addListener(new ProjectObserver.Listener() {
			
			private void refreshOpenDocs(IJavaProject project) {
				if (project.getProjectBuild() != null && project.getProjectBuild().getBuildFile() != null ) {
					URI projectBuildFileUri = project.getProjectBuild().getBuildFile();
					if (projectBuildFileUri.toString().endsWith(POM_XML)) {
						server.getTextDocumentService().getAll().stream()
							.map(doc -> URI.create(doc.getId().getUri()))
							.filter(projectBuildFileUri::equals)
							.findFirst().ifPresent(docPath -> {
								log.info("Refresh inlays for: " + docPath);
								server.getClient().refreshInlayHints();
							});
					}
				}
			}
			
			@Override
			public void deleted(IJavaProject project) {
				refreshOpenDocs(project);
			}
			
			@Override
			public void created(IJavaProject project) {
				refreshOpenDocs(project);
			}
			
			@Override
			public void changed(IJavaProject project) {
				refreshOpenDocs(project);
			}
		});

	}

	@Override
	public List<InlayHint> handle(CancelChecker token, InlayHintParams params) {
		URI uri = URI.create(params.getTextDocument().getUri());
		if ("file".equals(uri.getScheme()) && POM_XML.equals(Paths.get(uri).getFileName().toString())) {
			
			log.info("INLAY for " + uri);
			
			Optional<IJavaProject> projectOpt = projectFinder.find(params.getTextDocument());
			if (projectOpt.isPresent()) {

				URI buildFileUri = projectOpt.get().getProjectBuild().getBuildFile();
				
				if (buildFileUri.equals(uri) && SpringProjectUtil.isBootProject(projectOpt.get())) {
					Version currentVersion = SpringProjectUtil.getSpringBootVersion(projectOpt.get());
					if (currentVersion != null) {
						try {
							ResolvedSpringProject genProject = generationsProvider.getProject(SpringProjectUtil.SPRING_BOOT);
							if (genProject != null) {
								Generation generation = GenerationsValidator.getGenerationForJavaProject(projectOpt.get(), genProject);
								if (generation != null && VersionValidationUtils.isOssValid(generation)) {
									TextDocument doc = server.getTextDocumentService().getLatestSnapshot(params.getTextDocument().getUri());
									
									if (doc != null) {
										String content = doc.get();
										
										if (!content.isEmpty()) {
											// if doc is not empty, dive into the details and provide more sophisticated content assist proposals
											DOMParser parser = DOMParser.getInstance();
											DOMDocument dom = parser.parse(content, "", null);
											
											DOMElement project = dom.getDocumentElement();
											if (project != null && "project".equals(project.getTagName())) {
												for (int j = 0; j < project.getChildNodes().getLength(); j++) {
													var child = project.getChildNodes().item(j);
													if ("dependencies".equals(child.getNodeName()) && child instanceof DOMElement dependencies) {
														try {
															Command command = new Command();
															command.setTitle("Add Spring Boot Starters");
															command.setCommand("spring.initializr.addStarters");
															
															InlayHintLabelPart label = new InlayHintLabelPart("Add Spring Boot Starters...");
															label.setCommand(command);
															
															InlayHint hint = new InlayHint();
															hint.setPosition(doc.toPosition(dependencies.getStartTagCloseOffset() + 1));
															hint.setKind(InlayHintKind.Parameter);
															hint.setPaddingLeft(true);
															hint.setLabel(List.of(label));
															
															log.info("Sending inlay for " + uri);
															return List.of(hint);
														} catch (Exception e) {
															log.error("", e);
															break;
														}
													}
												}
											}
											

										}
									}
								}
							}
						} catch (Exception e) {
							log.error("", e);
						}
						
					}
				}
			}
			
		}
		return Collections.emptyList();
	}

}
