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
package org.springframework.ide.vscode.commons.languageserver.definition;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.core.publisher.Flux;

/**
 * {@link SimpleDefinitionFinder} provides a 'dummy' implementation of
 * @author Kris De Volder
 */
public class SimpleDefinitionFinder<T extends SimpleLanguageServer> implements DefinitionHandler {

	protected final T server;

	public SimpleDefinitionFinder(T server) {
		this.server = server;
	}

	@Override
	public List<Location> handle(TextDocumentPositionParams position) {
		return findDefinitions(position)
				.collect(Collectors.toList()).block();
	}

	/**
	 * This is meant to be overridden by subclass. This method provides a simple implementation
	 * of 'goto definition' (which is not one you probably want to use in practice, but it
	 * might be usful just to test whether things are wired up correctly to make the
	 * 'goto definition' action in vscode work.
	 * <p>
	 * The implementation provided here simply looks for the first occurrence of the word
	 * currently pointed at in the current document using String.indexOf.
	 */
	protected Flux<Location> findDefinitions(TextDocumentPositionParams params) {
		try {
			TextDocument doc = server.getTextDocumentService().get(params);
			if (doc != null) {
				int offset = doc.toOffset(params.getPosition());
				int start = offset;
				while (Character.isLetter(doc.getSafeChar(start))) {
					start--;
				}
				start = start+1;
				int end = offset;
				while (Character.isLetter(doc.getSafeChar(end))) {
					end++;
				}
				String word = doc.textBetween(start, end);
				Log.log("Looking for definition of '"+word+"'");
				String text = doc.get();
				int def = text.indexOf(word);
				if (def>=0) {
					return Flux.just(
						new Location(params.getTextDocument().getUri(),
							doc.toRange(def, word.length())
						)
					)
					.doOnNext((Location loc) -> {
						Log.log("definition: "+loc);
					});
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Flux.empty();
	}

}
