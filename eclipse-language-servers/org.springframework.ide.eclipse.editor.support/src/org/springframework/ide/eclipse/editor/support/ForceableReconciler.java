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
package org.springframework.ide.eclipse.editor.support;

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

/**
 * We unforytunately must subclass this just to make it possible to call non
 * public method 'forceReconcile'.
 * <p>
 * We need this to be able to force a reconcile at certain times (e.g. when
 * some underlying data defining the schema has changed).
 */
public class ForceableReconciler extends MonoReconciler {

	public ForceableReconciler(IReconcilingStrategy strategy) {
		super(strategy, false);
	}

	public void forceReconcile() {
		super.forceReconciling();
	}

}