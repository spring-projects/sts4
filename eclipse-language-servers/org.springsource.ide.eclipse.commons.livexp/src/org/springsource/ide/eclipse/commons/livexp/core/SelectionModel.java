/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import org.eclipse.core.runtime.Assert;

/**
 * A model capable of holding a value (selected by the user in a UI)
 * of a given type and some validation logic associated with that value.
 */
public class SelectionModel<T> {

	public final LiveVariable<T> selection;
	public final LiveExpression<ValidationResult> validator;

	public SelectionModel(LiveVariable<T> selection,
			LiveExpression<ValidationResult> validator) {
		super();
		Assert.isNotNull(selection);
		Assert.isNotNull(validator);
		this.selection = selection;
		this.validator = validator;
	}

	/**
	 * Convenience constructor for creating selection model without
	 * Validation.
	 */
	public SelectionModel(LiveVariable<T> selection) {
		this(selection, Validator.OK);
	}

	public SelectionModel() {
		this(new LiveVariable<T>());
	}

}
