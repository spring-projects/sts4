/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.util;

import java.util.function.Predicate;

import org.springsource.ide.eclipse.commons.core.util.StringUtil;

public class StringMatchers {

	public static final Predicate<String> ACCEPT_ALL = (s) -> true;

	public static Predicate<String> caseInsensitiveSubstring(String _substring) {
		if (StringUtil.hasText(_substring)) {
			String substring = _substring.trim().toLowerCase();
			return (s) -> s.toLowerCase().contains(substring);
		}
		return ACCEPT_ALL;
	}

}
