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
 * This content assist handler handles a Java qualified name change entered in
 * the text control specified by this handler.
 * <p>
 * An option browse button can also be specified by this handler which may also
 * trigger a qualified name change event, depending on whether the content
 * assist adapter that invokes this handler supports browse button functionality
 * </p>
 * @author Nieraj Singh
 */
public interface IJavaContentAssistHandler {

	/**
	 * Optional browse button that can be adapted to support Java type browsing.
	 * Clicking the browse button triggers a Java qualified name event.
	 * 
	 * @return browse button to adapt, or null if no browse functionality is
	 *        needed
	 */
	public Button getBrowseButtonControl();

	/**
	 * Text control to adapt to support Java content assist as well as trigger
	 * Java qualified name change events. Must not be null.
	 * 
	 * @return text control to adapt. Must not be null
	 */
	public Text getJavaTextControl();

	/**
	 * Shell to use for the browse button, and content assist on the text
	 * control. Must not be null.
	 * 
	 * @return shell to use for browse button and content assist. Cannot be
	 *        null.
	 */
	public Shell getShell();

	/**
	 * Concrete subclass get notified when a Java qualified name is entered in
	 * the given text control, or optionally, selected via the browse button
	 * press.
	 * 
	 * @param qualifiedName
	 */
	public void handleJavaTypeSelection(String qualifiedName);

}
