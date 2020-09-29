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
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;

public class RunTargetTypes {

	//TODO: Get rid of the 'LOCAL' contstants in this class.
	// The existence of this class and all the references littered around the
	// code pointing to its constants makes it rather hard in some cases to
	// mock things out for unit testing.
	public static final RunTargetType<Void> LOCAL = new LocalRunTargetType("Local");
}
