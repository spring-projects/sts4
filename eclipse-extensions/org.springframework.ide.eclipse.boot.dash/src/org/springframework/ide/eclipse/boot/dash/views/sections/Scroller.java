/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.springsource.ide.eclipse.commons.livexp.ui.Reflowable;

public class Scroller extends SharedScrolledComposite implements Reflowable {

	public Scroller(Composite parent) {
		this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	}

	public Scroller(Composite parent, int style) {
		super(parent, style);

		setFont(parent.getFont());

//		fToolkit= JavaPlugin.getDefault().getDialogsFormToolkit();

		setExpandHorizontal(true);
		setExpandVertical(true);

		Composite body= new Composite(this, SWT.NONE);
		body.setFont(parent.getFont());
		setContent(body);
	}

	public Composite getBody() {
		return (Composite) getContent();
	}

	@Override
	public boolean reflow() {
		reflow(true);
		return true;
	}

}
