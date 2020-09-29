/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.JavaParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.AbstractJavaContentAsssitHandler;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.IJavaContentAssistHandler;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.JavaContentAssistUIAdapter;


/**
 * Editor that creates controls for entering Java types, as well as browsing for
 * types. Note that this requires a Java UI adapter that supports content
 * assist, adapts controls to support content assist, as well as support for
 * browsing Java types. If no adapter is provider, this editor will have a
 * regular text control with no browse button.
 * @author Nieraj Singh
 */
public class JavaParameterEditor extends BaseParameterEditor {

	private JavaContentAssistUIAdapter adapter;

	public JavaParameterEditor(ICommandParameter parameter,
			JavaContentAssistUIAdapter adapter, boolean requiresLabel) {
		super(parameter, requiresLabel);
		this.adapter = adapter;
	}

	public Composite createControls(Composite parent) {
		Composite parameterArea = super.createControls(parent);
		// Must have a valid adapter to enable the browse button.
		if (adapter == null) {
			return parameterArea;
		}

		int numberOfControls = 1;

		Composite fieldComposite = new Composite(parameterArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(numberOfControls)
				.applyTo(fieldComposite);
		GridDataFactory.fillDefaults().grab(false, false)
				.applyTo(fieldComposite);

		Button browse = new Button(fieldComposite, SWT.PUSH);

		browse.setEnabled(true);
		browse.setText("Browse...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(browse);

		GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
		data.heightHint = getButtonHeight();
		
		browse.setLayoutData(data);

		IJavaContentAssistHandler handler = new AbstractJavaContentAsssitHandler(
				getTextControl(), browse) {

			public void handleJavaTypeSelection(String qualifiedName) {
				setParameterValueAndNotifyClear(qualifiedName);
			}
		};

		adapter.adapt(handler);

		return fieldComposite;
	}
	
	protected int getButtonHeight() {
		return 21;
	}

	protected JavaParameterDescriptor getJavaParameterDescriptor() {
		ICommandParameterDescriptor parameterDescriptor = getParameterDescriptor();
		if (parameterDescriptor instanceof JavaParameterDescriptor) {
			return (JavaParameterDescriptor) parameterDescriptor;
		}
		return null;
	}

}
