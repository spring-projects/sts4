/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;
import org.springsource.ide.eclipse.commons.livexp.ui.util.SwtConnect;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class RemoteAppsSection extends PrefsPageSection {

	private RemoteAppsPrefs prefs = new RemoteAppsPrefs();
	private Text text;
	private static final int HEIGHT_HINT = 150;

	private LiveVariable<String> model = new LiveVariable<>("");
	private LiveExpression<ValidationResult> validator = new AsyncLiveExpression<ValidationResult>(ValidationResult.OK) {
		{
			dependsOn(model);
		}
		@Override
		protected ValidationResult compute() {
			try {
				RemoteAppsPrefs.parse(model.getValue());
				return ValidationResult.OK;
			} catch (Exception e) {
				return ValidationResult.error(ExceptionUtil.getMessage(e));
			}
		}
	};

	public RemoteAppsSection(IPageWithSections owner) {
		super(owner);
	}

	@Override
	public boolean performOK() {
		prefs.setRawJson(model.getValue());
		return true;
	}

	@Override
	public void performDefaults() {
		model.setValue("");
	}

	@Override
	public void createContents(Composite parent) {
		model.setValue(prefs.getRawJson());
		text = new Text(parent, SWT.BORDER|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL|SWT.WRAP);
		GridDataFactory.fillDefaults()
		.hint(UIConstants.FIELD_TEXT_AREA_WIDTH, HEIGHT_HINT)
		.grab(true, false)
		.applyTo(text);
		SwtConnect.connect(text, model);
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

}
