/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover;

import static org.springframework.ide.vscode.boot.java.utils.ASTUtils.nameRange;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class ActiveProfilesProvider implements HoverProvider {

	@Override
	public Hover provideHover(
			ASTNode node,
			Annotation annotation,
			ITypeBinding type,
			int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps
	) {
		if (runningApps.length>0) {
			StringBuilder markdown = new StringBuilder();
			markdown.append("**Active Profiles**\n\n");
			boolean hasInterestingApp = false;
			for (SpringBootApp app : runningApps) {
				List<String> profiles = app.getActiveProfiles();
				if (profiles==null) {
					markdown.append(LiveHoverUtils.niceAppName(app)+" : _Unknown_\n\n");
				} else {
					hasInterestingApp = true;
					if (profiles.isEmpty()) {
						markdown.append(LiveHoverUtils.niceAppName(app)+" : _None_\n\n");
					} else {
						markdown.append(LiveHoverUtils.niceAppName(app)+" :\n");
						for (String profile : profiles) {
							markdown.append("- "+profile+"\n");
						}
						markdown.append("\n");
					}
				}
			}
			if (hasInterestingApp) {
				return new Hover(
						ImmutableList.of(Either.forLeft(markdown.toString()))
				);
			}
		}
		return null;
	}

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		if (runningApps.length > 0) {
			Builder<Range> ranges = ImmutableList.builder();
			nameRange(doc, annotation).ifPresent(ranges::add);

			Set<String> allActiveProfiles = getAllActiveProfiles(runningApps);
			annotation.accept(new ASTVisitor() {
				@Override
				public boolean visit(StringLiteral node) {
					String value = ASTUtils.getLiteralValue(node);
					if (value!=null && allActiveProfiles.contains(value)) {
						rangeOf(doc, node).ifPresent(ranges::add);
					}
					return true;
				}
			});
			return ranges.build();
		}
		return ImmutableList.of();
	}

	private static Set<String> getAllActiveProfiles(SpringBootApp[] runningApps) {
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		for (SpringBootApp app : runningApps) {
			List<String> profiles = app.getActiveProfiles();
			if (profiles!=null) {
				builder.addAll(app.getActiveProfiles());
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
			Log.log(e);
			return Optional.empty();
		}
	}

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	@Override
	public Collection<Range> getLiveHoverHints(TypeDeclaration typeDeclaration, TextDocument doc,
			SpringBootApp[] runningApps) {
		return null;
	}

}
