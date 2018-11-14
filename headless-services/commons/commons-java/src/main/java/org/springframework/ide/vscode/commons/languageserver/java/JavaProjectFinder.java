/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * Java project finder provides a means to obtain the project context associated with
 * a document location.
 *  *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public interface JavaProjectFinder {

	Optional<IJavaProject> find(TextDocumentIdentifier doc);

	default JavaProjectFinder filter(Predicate<IJavaProject> acceptWhen) {
		return doc -> this.find(doc).flatMap(jp -> {
			if (acceptWhen.test(jp)) {
				return Optional.of(jp);
			}
			return Optional.empty();
		});
	}

}
