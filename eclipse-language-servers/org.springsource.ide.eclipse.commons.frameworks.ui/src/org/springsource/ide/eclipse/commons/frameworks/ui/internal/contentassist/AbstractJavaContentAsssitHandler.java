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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author Nieraj Singh
 */
public abstract class AbstractJavaContentAsssitHandler implements IJavaContentAssistHandler {

	private Button browseButton;
	private Text javaText;

	public AbstractJavaContentAsssitHandler(Text javaText, Button browseButton) {
		this.browseButton = browseButton;
		this.javaText = javaText;
	}

	/* (non-Javadoc)
	 * @see com.springsource.sts.frameworks.ui.internal.parameters.editors.IJavaContentAssistHandler#getBrowseButtonControl()
	 */
	public Button getBrowseButtonControl() {
		return browseButton;
	}

	/* (non-Javadoc)
	 * @see com.springsource.sts.frameworks.ui.internal.parameters.editors.IJavaContentAssistHandler#getJavaTextControl()
	 */
	public Text getJavaTextControl() {
		return javaText;
	}

	/* (non-Javadoc)
	 * @see com.springsource.sts.frameworks.ui.internal.parameters.editors.IJavaContentAssistHandler#getShell()
	 */
	public Shell getShell() {
		// No null check. This should not be null.
		return javaText.getShell();
	}

}
