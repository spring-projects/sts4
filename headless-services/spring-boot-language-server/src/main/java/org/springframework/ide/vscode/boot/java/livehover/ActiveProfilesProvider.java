/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import static org.springframework.ide.vscode.boot.java.utils.ASTUtils.nameRange;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class ActiveProfilesProvider implements HoverProvider {

	private static final Logger log = LoggerFactory.getLogger(ActiveProfilesProvider.class);

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {

		if (processLiveData.length > 0) {
			StringBuilder markdown = new StringBuilder();
			markdown.append("**Active Profiles**\n\n");
			boolean hasInterestingApp = false;
			for (SpringProcessLiveData liveData : processLiveData) {
				String[] profiles = liveData.getActiveProfiles();
				if (profiles == null) {
					markdown.append(LiveHoverUtils.niceAppName(liveData)+" : _Unknown_\n\n");
				} else {
					hasInterestingApp = true;
					if (profiles.length == 0) {
						markdown.append(LiveHoverUtils.niceAppName(liveData)+" : _None_\n\n");
					} else {
						markdown.append(LiveHoverUtils.niceAppName(liveData)+" :\n");
						for (String profile : profiles) {
							markdown.append("- "+profile+"\n");
						}
						markdown.append("\n");
					}
				}
			}
			if (hasInterestingApp) {
				Hover hover = new Hover(
						ImmutableList.of(Either.forLeft(markdown.toString()))
				);
				if (hover != null) {
					Optional<Range> optional = nameRange(doc, annotation);
					if (optional.isPresent()) {
						hover.setRange(optional.get());
					}
				}
				return hover;
			}
		}
		return null;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		if (processLiveData.length > 0) {
			Builder<CodeLens> codeLenses = ImmutableList.builder();

			Set<String> allActiveProfiles = getAllActiveProfiles(processLiveData);
			if (allActiveProfiles != null && allActiveProfiles.size() > 0) {
				nameRange(doc, annotation).map(CodeLens::new).ifPresent(codeLenses::add);
			}

			annotation.accept(new ASTVisitor() {
				@Override
				public boolean visit(StringLiteral node) {
					String value = ASTUtils.getLiteralValue(node);
					if (value!=null && allActiveProfiles.contains(value)) {
						rangeOf(doc, node).map(CodeLens::new).ifPresent(codeLenses::add);
					}
					return true;
				}
			});
			return codeLenses.build();
		}
		return ImmutableList.of();
	}

	private static Set<String> getAllActiveProfiles(SpringProcessLiveData[] processLiveData) {
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		for (SpringProcessLiveData liveData : processLiveData) {
			String[] profiles = liveData.getActiveProfiles();
			if (profiles != null) {
				builder.addAll(Arrays.asList(liveData.getActiveProfiles()));
			}
		}
		return builder.build();
	}

	private static Optional<Range> rangeOf(TextDocument doc, StringLiteral node) {
		try {
			int start = node.getStartPosition();
			int end = start + node.getLength();
			if (doc.getSafeChar(start)=='"') {
				start++;
			}
			if (doc.getSafeChar(end-1)=='"') {
				end--;
			}
			return Optional.of(doc.toRange(start, end-start));
		} catch (Exception e) {
			log.error("", e);
			return Optional.empty();
		}
	}

}
