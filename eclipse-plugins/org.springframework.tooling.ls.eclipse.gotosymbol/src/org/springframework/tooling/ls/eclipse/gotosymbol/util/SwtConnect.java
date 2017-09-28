/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.util;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Static helper methods for attaching LiveExps to SWT widgets.
 */
public class SwtConnect {

	public static void connect(Text widget, LiveVariable<String> model) {
		ModifyListener widgetListener = (ModifyEvent e) -> {
			model.setValue(widget.getText());
		};
		ValueListener<String> modelListener = new UIValueListener<String>() {
			@Override
			protected void uiGotValue(LiveExpression<String> exp, String value) {
				String newText = model.getValue();
				if (newText==null) {
					newText = "";
				}
				String oldText = widget.getText();
				if (!newText.equals(oldText)) {
					widget.setText(newText);
				}
			}
		};
		widget.addModifyListener(widgetListener);
		model.addListener(modelListener);
		widget.addDisposeListener(e -> {
			model.removeListener(modelListener);
		});
		
	}

	public static void connect(Label widget, LiveExpression<String> model) {
		ValueListener<String> modelListener = new UIValueListener<String>() {
			@Override
			protected void uiGotValue(LiveExpression<String> exp, String value) {
				String newText = model.getValue();
				if (newText==null) {
					newText = "";
				}
				widget.setText(newText);
			}
		};
		model.addListener(modelListener);
		widget.addDisposeListener(xx -> model.removeListener(modelListener));
	}

}
