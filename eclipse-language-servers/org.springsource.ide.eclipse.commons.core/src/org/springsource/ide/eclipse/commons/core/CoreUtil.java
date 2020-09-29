/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.util.Properties;

import org.eclipse.core.runtime.Assert;

/**
 * @author Steffen Pingel
 */
public class CoreUtil {

	/**
	 * Replaces placeholders in <code>text</code> with values from
	 * <code>properties</code>. Placeholders use the following format:
	 * <code>${key}</code>. If the key is not found in <code>properties</code>
	 * the placeholder is retained.
	 * 
	 * @param text the text
	 * @param properties key value pairs for substitution
	 * @return the substituted text
	 */
	public static String substitute(String text, Properties properties) {
		Assert.isNotNull(text);
		Assert.isNotNull(properties);
		String[] segments = text.split("\\$\\{");
		StringBuffer sb = new StringBuffer(text.length());
		sb.append(segments[0]);
		for (int i = 1; i < segments.length; i++) {
			String segment = segments[i];
			String substitution = null;
			int brace = segment.indexOf('}');
			if (brace > 0) {
				String keyword = segment.substring(0, brace);
				substitution = properties.getProperty(keyword);
			}

			if (substitution != null) {
				sb.append(substitution);
				sb.append(segment.substring(brace + 1));
			}
			else {
				sb.append("${");
				sb.append(segment);
			}
		}
		return sb.toString();
	}

}
