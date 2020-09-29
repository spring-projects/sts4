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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Simple progress indicator that indicates liveliness, has a label and a stop
 * button to make invisible. All operations must be invoked from UI thread.
 * @author Nieraj Singh
 */
public class ProgressIndicatorWithStop {

	private ProgressIndicator refreshProgressIndicator;
	private ToolBar toolBar;
	private Label refreshLabel;
	private String progressMessage;
	private boolean isCancellable = true;

	public ProgressIndicatorWithStop(String progressLabel, Composite parent,
			boolean isCancellable) {
		this.progressMessage = progressLabel;
		this.isCancellable = isCancellable;
		createStatusArea(parent);
	}

	protected void createStatusArea(Composite parent) {
		if (parent == null) {
			return;
		}
		Composite statusArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(statusArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusArea);

		refreshLabel = new Label(statusArea, SWT.CENTER);
		GridDataFactory.fillDefaults().grab(false, false)
				.align(SWT.LEFT, SWT.CENTER).applyTo(refreshLabel);
		refreshLabel.setText(progressMessage != null ? progressMessage : "");

		refreshProgressIndicator = new ProgressIndicator(statusArea);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
				.grab(true, false).applyTo(refreshProgressIndicator);

		toolBar = new ToolBar(statusArea, SWT.FLAT | SWT.NO_FOCUS);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
				.grab(true, false).applyTo(toolBar);

		if (isCancellable) {
			ToolItem item = new ToolItem(toolBar, SWT.NONE);

			item.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_ELCL_STOP));
			item.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					stop();
				}

			});
		}

		setControlVisibility(false);
	}

	/**
	 * Can only be invoked on UI thread
	 */
	public void start() {
		if (!refreshProgressIndicator.isDisposed()
				&& !refreshProgressIndicator.isVisible()) {
			refreshProgressIndicator.beginAnimatedTask();
			setControlVisibility(true);
		}
	}

	protected void setControlVisibility(boolean visibility) {
		if (!refreshLabel.isDisposed()) {
			refreshLabel.setVisible(visibility);
		}
		if (!toolBar.isDisposed()) {
			toolBar.setVisible(visibility);
		}
		if (!refreshProgressIndicator.isDisposed()) {
			refreshProgressIndicator.setVisible(visibility);
		}
	}

	/**
	 * Can only be invoked on UI thread
	 */
	public void finish() {
		if (!refreshProgressIndicator.isDisposed()
				&& refreshProgressIndicator.isVisible()) {
			refreshProgressIndicator.done();
			setControlVisibility(false);
		}
	}

	/**
	 * Clients can override. Peforms an action when stop button is pressed.
	 */
	public void stop() {
		finish();
	}

}
