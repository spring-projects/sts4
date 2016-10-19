/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.yaml.util;

import java.io.InputStream;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.util.HtmlSnippet;

import static org.springframework.ide.vscode.util.HtmlSnippet.*;

/**
 * Static methods and convenience constants for creating some 'description providers'.
 *
 * @author Kris De Volder
 */
public class DescriptionProviders {
	
	final static Logger logger = LoggerFactory.getLogger(DescriptionProviders.class);

	public static final Provider<HtmlSnippet> NO_DESCRIPTION = () -> italic(text("no description"));

	public static Provider<HtmlSnippet> snippet(final HtmlSnippet snippet) {
		return new Provider<HtmlSnippet>() {
			@Override
			public String toString() {
				return snippet.toString();
			}
			@Override
			public HtmlSnippet get() {
				return snippet;
			}
		};
	}

	public static Provider<HtmlSnippet> fromClasspath(final Class<?> klass, final String resourcePath) {
		return new Provider<HtmlSnippet>() {
			@Override
			public String toString() {
				return "DescriptionFromClassPth(class="+klass.getSimpleName()+", "+resourcePath+")";
			}
			@Override
			public HtmlSnippet get() {
				try {
					InputStream stream = klass.getResourceAsStream(resourcePath);
					if (stream!=null) {
						return HtmlSnippet.text(IOUtil.toString(stream));
					}
				} catch (Exception e) {
					logger.error("Error", e);;
				}
				return NO_DESCRIPTION.get();
			}
		};
	}
}
