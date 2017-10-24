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
package org.springframework.ide.vscode.boot.java.profiles;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class ActiveProfilesProvider implements HoverProvider {

	public static final String ANNOTATION = "org.springframework.context.annotation.Profile";

	@Override
	public CompletableFuture<Hover> provideHover(
			ASTNode node,
			Annotation annotation,
			ITypeBinding type,
			int offset,
			TextDocument doc, SpringBootApp[] runningApps
	) {
		if (runningApps.length>0) {
			StringBuilder markdown = new StringBuilder();
			markdown.append("**Active Profiles**\n\n");
			for (SpringBootApp app : runningApps) {
				List<String> profiles = app.getActiveProfiles();
				if (profiles==null) {
					markdown.append(niceAppName(app)+" : _Unknown_\n\n");
				} else if (profiles.isEmpty()) {
					markdown.append(niceAppName(app)+" : _None_\n\n");
				} else {
					markdown.append(niceAppName(app)+" :\n");
					for (String profile : profiles) {
						markdown.append("- "+profile+"\n");
					}
					markdown.append("\n");
				}
			}
			return CompletableFuture.completedFuture(new Hover(
					ImmutableList.of(Either.forLeft(markdown.toString()))
			));
		}
		return null;
	}

	private String niceAppName(SpringBootApp app) {
		return "Process [PID="+app.getProcessID()+", name=`"+app.getProcessName()+"`]";
	}

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			if (runningApps.length > 0) {
				Name node = annotation.getTypeName();
				return ImmutableList.of(doc.toRange(node.getStartPosition(), node.getLength()));
			}
		} catch (BadLocationException e) {
			Log.log(e);
		}
		return ImmutableList.of();
	}
}
