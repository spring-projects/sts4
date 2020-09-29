/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

/**
 * Interface to be implemented by content elements that can provide a 'nice' name
 * to display to the user. Typically this method will be used by label providers
 * and other UI widgetry.
 * 
 * @author Kris De Volder
 */
public interface DisplayNameable {

	String getDisplayName();
	
}
