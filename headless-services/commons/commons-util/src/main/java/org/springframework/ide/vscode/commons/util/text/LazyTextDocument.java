/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util.text;

import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

import org.springframework.ide.vscode.commons.util.IOUtil;

import javolution.text.Text;

public class LazyTextDocument extends TextDocument {
	
	private boolean loaded = false;
	private Supplier<String> loader;
	
	public LazyTextDocument(String uri, LanguageId languageId, Supplier<String> loader) {
		super(uri, languageId);
		this.loader = loader;
	}
	
	public LazyTextDocument(String uri, LanguageId languageId) {
		this(uri, LanguageId.JAVA, () -> {
			try {
				InputStream stream = URI.create(uri).toURL().openStream();
				return IOUtil.toString(stream);
			} catch (Exception e) {
				return null;
			}
		});
	}

	@Override
	public String get() {
		// TODO Auto-generated method stub
		return super.get();
	}

	@Override
	protected synchronized Text getText() {
		if (!loaded) {
			setText(loader.get());
			loaded = true;
		}
		return super.getText();
	}

}
