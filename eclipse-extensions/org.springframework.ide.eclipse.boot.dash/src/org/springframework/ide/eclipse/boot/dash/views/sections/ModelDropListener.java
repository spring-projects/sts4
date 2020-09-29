/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ModelDropListener extends DropTargetAdapter {

	private final UserInteractions ui;
	private final ModifiableModel modifiableModel;
	private final BootDashModel model;

	private IStructuredSelection selection;
	private Object target;
	private boolean canDrop = false;
	private int overrideDropOp = DND.DROP_COPY;

	public ModelDropListener(BootDashModel model, UserInteractions ui) {
		this.ui = ui;
		this.modifiableModel = model instanceof ModifiableModel ? (ModifiableModel) model : null;
		this.model = model;
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (canDrop) {
			Job job = new Job("Performing deployment to " + model.getRunTarget().getName()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (modifiableModel != null && selection != null) {
						Object[] selectionObjs = selection.toArray();
						try {
							modifiableModel.add(Arrays.asList(selectionObjs));

						} catch (Exception e) {
							ui.errorPopup("Failed to Add Element", e.getMessage());
						}
					}
					return Status.OK_STATUS;
				}

			};
			job.schedule();
		}

		super.drop(event);
	}


	@Override
	public void dropAccept(DropTargetEvent event) {

		int operations = event.operations;
		canDrop = false;
		if (modifiableModel != null && (operations == DND.DROP_COPY || operations == DND.DROP_DEFAULT || overrideDropOp == DND.DROP_COPY)) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
				selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
				Object[] selectionObjs = selection.toArray();

				if (selectionObjs != null) {
					canDrop = modifiableModel.canBeAdded(Arrays.asList(selectionObjs));
				}
			}
		}
		super.dropAccept(event);
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		this.overrideDropOp = event.operations;
		if (event.detail == DND.DROP_DEFAULT || event.detail == DND.DROP_NONE) {
			event.detail = DND.DROP_COPY;
			this.overrideDropOp = DND.DROP_COPY;
		}
		super.dragEnter(event);
	}

}