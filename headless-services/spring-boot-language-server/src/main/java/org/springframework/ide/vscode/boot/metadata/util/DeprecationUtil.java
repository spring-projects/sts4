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
package org.springframework.ide.vscode.boot.metadata.util;

import java.util.Optional;

import org.springframework.ide.vscode.boot.configurationmetadata.Deprecation;
import org.springframework.ide.vscode.commons.java.IAnnotatable;
import org.springframework.ide.vscode.commons.java.IJavaElement;

import com.google.common.collect.ImmutableSet;

public class DeprecationUtil {

	private static final ImmutableSet<String> DEPRECATED_ANOT_NAMES = ImmutableSet.of(
			"org.springframework.boot.context.properties.DeprecatedConfigurationProperty",
			"DeprecatedConfigurationProperty",
			"java.lang.Deprecated",
			"Deprecated"
	);

	/**
	 * Extract {@link Deprecation} info from annotations on a {@link IJavaElement}
	 */
	public static Deprecation extract(IJavaElement je) {
		Optional<Deprecation> deprecation = Optional.empty();
		if (je instanceof IAnnotatable) {
			deprecation = extract((IAnnotatable)je);
		}
		return deprecation.isPresent() ? deprecation.get() : null;
	}

	/**
	 * Extract {@link Deprecation} info from annotations on a {@link IJavaElement}
	 */
	private static Optional<Deprecation> extract(IAnnotatable m) {
		return m.getAnnotations().filter(a -> DEPRECATED_ANOT_NAMES.contains(a.fqName())).map(a -> {
			Deprecation d = new Deprecation();
			a.getMemberValuePairs().forEach(pair -> {
				String name = pair.getMemberName();
				if (name.equals("reason")) {
					d.setReason((String) pair.getValue());
				} else if (name.equals("replacement")) {
					d.setReplacement((String) pair.getValue());
				}
			});
			return d;
		}).findFirst();
	}

}
