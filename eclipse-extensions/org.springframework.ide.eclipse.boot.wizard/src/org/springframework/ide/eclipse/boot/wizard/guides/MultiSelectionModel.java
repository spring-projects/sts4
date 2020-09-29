/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.guides;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;

/**
 * Model of a selection that can be thought of as a Set of elements somehow selected by the user. 
 * 
 * @author Kris De Volder
 */
public class MultiSelectionModel<T> {

	public final LiveSet<T> selecteds;
	public final LiveExpression<ValidationResult> validator;

	public MultiSelectionModel(LiveSet<T> selecteds, LiveExpression<ValidationResult> validator) {
		this.selecteds = selecteds;
		this.validator = validator;
	}

}
