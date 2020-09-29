/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class MakeDefaultSection extends WizardPageSection {

	private Runnable clear;
	private Runnable makeDefault;

	public MakeDefaultSection(IPageWithSections owner, Runnable makeDefault, Runnable clear) {
		super(owner);
		this.makeDefault = makeDefault;
		this.clear = clear;
	}

	@Override
	public void createContents(Composite page) {

		final Composite composite = new Composite(page, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).spacing(5, SWT.DEFAULT).applyTo(composite);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER).applyTo(composite);

		Button makeDefaultButton = new Button(composite, SWT.PUSH);
		makeDefaultButton.setText("Make Default");
		makeDefaultButton.setToolTipText("Make currently selected dependencies selected by default");
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER).applyTo(makeDefaultButton);

		Button clearButton = new Button(composite, SWT.PUSH);
		clearButton.setText("Clear Selection");
		clearButton.setToolTipText("Clear dependencies selection");
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER).applyTo(clearButton);

		makeDefaultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (makeDefault != null) {
					makeDefault.run();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});

		clearButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (clear != null) {
					clear.run();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		});
	}

}
