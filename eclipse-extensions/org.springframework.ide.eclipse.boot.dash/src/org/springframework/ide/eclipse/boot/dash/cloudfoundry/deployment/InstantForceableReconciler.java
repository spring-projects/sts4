/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.springframework.ide.eclipse.editor.support.ForceableReconciler;

/**
 * Reconciler able to start reconciling almost instantly without waiting time
 * specified by the delay parameter before processing dirty regions. Input
 * document change will trigger instant reconcile. This is useful for single
 * viewer used for displaying content of different files for instance, file
 * input changes and reconcile happens instantly.
 *
 * @author Alex Boyko
 *
 */
public class InstantForceableReconciler extends ForceableReconciler {

	private final static int NO_DELAY = 1;

	/**
	 * Original delay. Default value must match the one specified in {@link AbstractReconciler}
	 */
	private int fOriginalDelay = 500;

	/**
	 * Stores original delay value when instant reconcile is performed, 0 otherwise
	 */
	private int fTempDelay = NO_DELAY;

	/**
	 * Creates instance of the reconciler
	 * @param strategy Reconcile strategy
	 */
	public InstantForceableReconciler(IReconcilingStrategy strategy) {
		super(strategy);
	}

	@Override
	protected void reconcilerDocumentChanged(IDocument document) {
		super.reconcilerDocumentChanged(document);
		// Force instant reconciling
		forceReconcileNow();
	}

	@Override
	public void setDelay(int delay) {
		super.setDelay(delay);
		fOriginalDelay = delay;
	}

	public void forceReconcileNow() {
		if (fOriginalDelay > NO_DELAY && fTempDelay == NO_DELAY) {
			// Remember original delay value
			int temp = fOriginalDelay;
			// Set delay to 0 to start processing dirty regions immediately
			setDelay(NO_DELAY);
			// Remember the original delay value to set it back when reconciling is complete
			fTempDelay = temp;
			forceReconcile();
		}
	}

	@Override
	protected void process(DirtyRegion dirtyRegion) {
		super.process(dirtyRegion);
		if (fTempDelay != NO_DELAY) {
			// If there is original delay stored set it back on now
			setDelay(fTempDelay);
			fTempDelay = NO_DELAY;
		}
	}

}
