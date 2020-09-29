/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.util.concurrent.Callable;

import org.springsource.ide.eclipse.commons.livexp.core.BooleanFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

/**
 * @author Kris De Volder
 */
public abstract class EditTemplateDialogModel implements OkButtonHandler {

	public final StringFieldModel template = new StringFieldModel("Template", getDefaultValue());
	public final FieldModel<Boolean> applyToAll = new BooleanFieldModel(getApplyToAllLabel(), getApplyToAllDefault());


	//TODO: This field can be a method once we adopt Java 8.
	public Callable<Void> restoreDefaultsHandler = new Callable<Void>() {
		public Void call() throws Exception {
			template.getVariable().setValue(getDefaultValue());
			return null;
		}
	};

	public abstract String getTitle();
	public abstract String getDefaultValue();
	public abstract String getHelpText();

	public abstract String getApplyToAllLabel();
	public abstract boolean getApplyToAllDefault();

}
