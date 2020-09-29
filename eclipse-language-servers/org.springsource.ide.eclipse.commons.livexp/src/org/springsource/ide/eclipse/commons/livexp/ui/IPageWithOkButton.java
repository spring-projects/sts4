/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

/**
 * An optional extension of IPageWithSections that some implementer may wish to implement.
 * Some section may make use of it to implement functionality that requires programatically
 * clicking the ok button.
 */
public interface IPageWithOkButton extends IPageWithSections {
	
	/**
	 * Simulate clicking the ok button (i.e. perform whatever action would be performed by clicking it 
	 * if the button is enabled or do nothing if it is disabled).
	 * 
	 * @return true if the button was clicked false if button was disabled.
	 */
	public boolean clickOk();

}
