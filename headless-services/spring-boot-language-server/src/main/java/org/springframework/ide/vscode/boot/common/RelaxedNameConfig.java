/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.common;

import org.springframework.ide.vscode.boot.metadata.types.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil.EnumCaseMode;

/**
 * Config object that determines some aspects of how the freedom of 'relaxed name binding'
 * are taken into account when generating content-assist completions.
 *
 * @author Kris De Volder
 */
public class RelaxedNameConfig {
	
	public static final RelaxedNameConfig ALIASSED = new RelaxedNameConfig(EnumCaseMode.ALIASED, BeanPropertyNameMode.ALIASED);
	public static final RelaxedNameConfig COMPLETION_DEFAULTS = new RelaxedNameConfig(EnumCaseMode.LOWER_CASE, BeanPropertyNameMode.HYPHENATED);

	private EnumCaseMode enumMode = EnumCaseMode.LOWER_CASE;
	private BeanPropertyNameMode beanMode = BeanPropertyNameMode.HYPHENATED;

	public RelaxedNameConfig(EnumCaseMode enumMode, BeanPropertyNameMode beanMode) {
		this.enumMode = enumMode;
		this.beanMode = beanMode;
	}
	public EnumCaseMode getEnumMode() {
		return enumMode;
	}
	public void setEnumMode(EnumCaseMode preferredEnumCompletions) {
		this.enumMode = preferredEnumCompletions;
	}
	public BeanPropertyNameMode getBeanMode() {
		return beanMode;
	}
	public void setBeanMode(BeanPropertyNameMode preferredBeanCompletions) {
		this.beanMode = preferredBeanCompletions;
	}

	@Override
	public String toString() {
		return "RelaxedNameConfig("+enumMode+", "+beanMode+")";
	}

}
