/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

	Collection<? extends IJavaProject> all();

	default JavaProjectFinder filter(Predicate<IJavaProject> acceptWhen) {

		final JavaProjectFinder delegate = this;

		return new JavaProjectFinder() {

			@Override
			public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
				return delegate.find(doc).flatMap(jp -> {
					if (acceptWhen.test(jp)) {
						return Optional.of(jp);
					}
					return Optional.empty();
				});
			}

			@Override
			public Collection<? extends IJavaProject> all() {
				return delegate.all().stream().filter(acceptWhen).collect(Collectors.toList());
			}

		};

	}

}
