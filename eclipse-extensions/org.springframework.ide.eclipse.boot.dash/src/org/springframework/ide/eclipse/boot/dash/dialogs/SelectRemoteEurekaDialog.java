/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashTreeLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashUnifiedTreeSection.BootModelViewerSorter;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

/**
 * @author Martin Lippert
 */
public class SelectRemoteEurekaDialog extends SelectionStatusDialog {

	private TreeViewer tv;
	private Stylers stylers;
	private ITreeContentProvider contentProvider;
	private Text manualEurekaURL;
	private Button enterManuallyButton;
	private Button selectRemoteButton;
	private BootDashViewModel model;
	private String choosenURL;

	public SelectRemoteEurekaDialog(Shell parent, ITreeContentProvider contentProvider) {
		super(parent);
		this.contentProvider = contentProvider;
	}

    @Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        createMessageArea(composite);

        selectRemoteButton = new Button(composite, SWT.RADIO);
        selectRemoteButton.setText("select remote Eureka instance");
        selectRemoteButton.setSelection(true);

    		tv = new TreeViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
    		tv.setSorter(new BootModelViewerSorter(this.model));
    		tv.getTree().setLinesVisible(true);
    		stylers = new Stylers(tv.getTree().getFont());
    		tv.setLabelProvider(new BootDashTreeLabelProvider(stylers, tv));
    		tv.setContentProvider(contentProvider);
    		tv.setInput(model);

    		GridData data = new GridData(GridData.FILL_BOTH);
    		data.widthHint = convertWidthInCharsToPixels(82);
    		data.heightHint = convertHeightInCharsToPixels(18);

    		Tree treeWidget = tv.getTree();
    		treeWidget.setLayoutData(data);
    		treeWidget.setFont(parent.getFont());

    		enterManuallyButton = new Button(composite, SWT.RADIO);
        enterManuallyButton.setText("use this remote Eureka URL");

        Label manualEurekaLabel = new Label(composite, SWT.NONE);
        manualEurekaLabel.setText("Eureka URL:");
        manualEurekaLabel.setEnabled(false);

        manualEurekaURL = new Text(composite, SWT.SINGLE | SWT.BORDER);
        manualEurekaURL.setEnabled(false);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.horizontalSpan = 3;
        manualEurekaURL.setLayoutData(gridData);

        SelectionListener radioGroupListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnabling();
				updateOKStatus();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		selectRemoteButton.addSelectionListener(radioGroupListener);
		enterManuallyButton.addSelectionListener(radioGroupListener);

		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				String url = getRemoteURL(event.getSelection());
				manualEurekaURL.setText(url);
				updateOKStatus();
			}
		});

		manualEurekaURL.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateOKStatus();
			}
		});

		updateOKStatus();
        return composite;
    }

	protected String getRemoteURL(ISelection selection) {
		if (selection instanceof TreeSelection) {
			Object selectedElement = ((TreeSelection) selection).getFirstElement();
			if (selectedElement instanceof BootDashElement) {
				String host = ((BootDashElement) selectedElement).getLiveHost();
				if (host != null && host.length() > 0) {
					if (!host.startsWith("http")) {
						host = "http://" + host;
					}
					return host;
				}
			}
		}
		return "";
	}

	protected void updateEnabling() {
		boolean remoteSelected = selectRemoteButton.getSelection();
		tv.getTree().setEnabled(remoteSelected);
		manualEurekaURL.setEnabled(!remoteSelected);
	}

	@Override
	protected void computeResult() {
		this.choosenURL = this.manualEurekaURL.getText();
	}

	protected void updateOKStatus() {
		IStatus status = null;

		if (selectRemoteButton.getSelection()) {
			ISelection selection = tv.getSelection();
			if (selection != null && !selection.isEmpty() && manualEurekaURL.getText() != null && manualEurekaURL.getText().length() > 0) {
				status = new Status(IStatus.OK, BootDashActivator.PLUGIN_ID, null);
			}
			else {
				status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "please select a remote Eureka instance");
			}
		}
		else {
			String manualURL = manualEurekaURL.getText();
			if (manualURL != null && manualURL.length() > 0) {
				try {
					URL url = new URL(manualURL);
					URI uri = url.toURI();
					if (uri.getHost() == null || uri.getHost().length() == 0) {
						status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "please enter a valid URL");
					}
					else {
						status = new Status(IStatus.OK, BootDashActivator.PLUGIN_ID, null);
					}
				} catch (URISyntaxException e) {
					status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "please enter a valid URL");
				} catch (MalformedURLException e) {
					status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "please enter a valid URL");
				}

			}
			else {
				status = new Status(IStatus.ERROR, BootDashActivator.PLUGIN_ID, "please enter URL of remote Eureka instance");
			}
		}

		updateStatus(status);
	}

	public void setInput(BootDashViewModel model) {
		this.model = model;
	}

	public String getSelectedEurekaURL() {
		return this.choosenURL;
	}

}
