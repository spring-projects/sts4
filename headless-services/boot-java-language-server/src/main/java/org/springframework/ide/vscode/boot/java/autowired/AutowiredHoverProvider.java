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
package org.springframework.ide.vscode.boot.java.autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class AutowiredHoverProvider implements HoverProvider {

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc, SpringBootApp[] runningApps) {
		return provideHover(annotation, doc, runningApps);
	}

	@Override
	public Range getLiveHoverHint(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			if (runningApps.length > 0) {

				List<String> liveBeanInfo = new ArrayList<>();
				for (SpringBootApp bootApp : runningApps) {
					try {
						String liveBeans = bootApp.getBeans();
						if (liveBeans != null && liveBeans.length() > 0) {
							liveBeanInfo.add(liveBeans);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (!liveBeanInfo.isEmpty()) {
					return getLiveHoverHint(annotation, doc, liveBeanInfo.toArray(new String[liveBeanInfo.size()]));
				}

				// TODO: real work
				Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
				return hoverRange;
			}
		}
		catch (BadLocationException e) {
			Log.log(e);
		}

		return null;
	}

	public Range getLiveHoverHint(Annotation annotation, TextDocument doc, String[] liveBeanJSON) {
		try {
			if (liveBeanJSON.length > 0) {
				// TODO: real work
				Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
				return hoverRange;
			}
		}
		catch (BadLocationException e) {
			Log.log(e);
		}

		return null;
	}

	private CompletableFuture<Hover> provideHover(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {

		try {
			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

			// TODO: real work

			Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
			Hover hover = new Hover();

			hover.setContents(hoverContent);
			hover.setRange(hoverRange);

			return CompletableFuture.completedFuture(hover);
		} catch (Exception e) {
			Log.log(e);
		}

		return null;
	}

}
