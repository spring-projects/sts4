/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

/**
 * Interface to be implemented by BDE that support 'duplication'.
 *
 * @author Kris De Volder
 */
public interface Duplicatable<T extends BootDashElement> extends BootDashElement {

	boolean canDuplicate();

	T duplicate(UserInteractions ui);

}
