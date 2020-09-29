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
package org.springsource.ide.eclipse.commons.livexp.ui;

/**
 * @author Kris De Volder
 */
public interface Reflowable {
	/**
	 * Returns true when reflow can stop here, false otherwise. Basically, this is
	 * to accomodate for Controls that have 'dynamic' refloability which may
	 * implement 'Reflowable' because they may or may not be reflowable, depending
	 * on how they are configured.
	 * <p>
	 * Such components may implement 'Reflowable' interface and return
	 * false asked to reflow when their current configuration means they
	 * aren't actually 'reflowable'.
	 */
	boolean reflow();
}
