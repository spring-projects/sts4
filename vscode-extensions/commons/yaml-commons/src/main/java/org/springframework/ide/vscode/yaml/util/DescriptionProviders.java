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

import static org.springframework.ide.vscode.yaml.util.Description.*;

/**
 * Static methods and convenience constants for creating some 'description providers'.
 *
 * @author Kris De Volder
 */
public class DescriptionProviders {
	
	final static Logger logger = LoggerFactory.getLogger(DescriptionProviders.class);

	public static final Provider<Description> NO_DESCRIPTION = () -> italic(text("no description"));

	public static Provider<Description> snippet(final Description snippet) {
		return new Provider<Description>() {
			@Override
			public String toString() {
				return snippet.toString();
			}
			@Override
			public Description get() {
				return snippet;
			}
		};
	}

	public static Provider<Description> fromClasspath(final Class<?> klass, final String resourcePath) {
		return new Provider<Description>() {
			@Override
			public String toString() {
				return "DescriptionFromClassPth(class="+klass.getSimpleName()+", "+resourcePath+")";
			}
			@Override
			public Description get() {
				try {
					InputStream stream = klass.getResourceAsStream(resourcePath);
					if (stream!=null) {
						return Description.text(IOUtil.toString(stream));
					}
				} catch (Exception e) {
					logger.error("Error", e);;
				}
				return NO_DESCRIPTION.get();
			}
		};
	}
}
