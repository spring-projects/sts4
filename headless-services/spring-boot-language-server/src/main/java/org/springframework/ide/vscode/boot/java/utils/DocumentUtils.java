/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class DocumentUtils {

	public static TextDocument getTempTextDocument(String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		TextDocument doc = docRef.get();
		if (doc == null) {
			doc = createTempTextDocument(docURI, content);
			docRef.set(doc);
		}
		return doc;
	}

	private static TextDocument createTempTextDocument(String docURI, String content) throws Exception {
		if (content == null) {
			Path path = new File(new URI(docURI)).toPath();
			content = new String(Files.readAllBytes(path));
		}

		TextDocument doc = new TextDocument(docURI, LanguageId.PLAINTEXT, 0, content);
		return doc;
	}

}
