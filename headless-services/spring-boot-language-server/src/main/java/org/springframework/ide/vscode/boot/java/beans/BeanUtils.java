/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import org.springframework.ide.vscode.commons.util.StringUtil;

public class BeanUtils {

	public static String getBeanName(String beanName) {
		if (StringUtil.hasText(beanName) && beanName.length() > 0 && Character.isUpperCase(beanName.charAt(0))) {
			// PT 162740382 - Special case: If more than one character is upper case, do not
			// convert bean name to starting lower case. only convert if name has one character
			// or the second character in the name is not upper case
			if (beanName.length() == 1 || !Character.isUpperCase(beanName.charAt(1))) {
				beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
			}
		}
		return beanName;
	}

}
