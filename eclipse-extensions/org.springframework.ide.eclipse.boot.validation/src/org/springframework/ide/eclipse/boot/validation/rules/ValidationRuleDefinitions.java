/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.rules;

import java.util.List;

import org.springframework.ide.eclipse.boot.validation.framework.IValidationRule;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

import com.google.common.collect.ImmutableList;

public class ValidationRuleDefinitions {

	public static List<ProblemType> getProblemTypes() {
		return ImmutableList.copyOf(BootValidationProblemType.values());
	}
	
	private static List<IValidationRule> rules = ImmutableList.of(
			(IValidationRule)new MissingConfigurationProcessorRule()
	);

	public static synchronized List<IValidationRule> getRuleDefinitions() {
		return rules;
	}

}
