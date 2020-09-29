/*******************************************************************************
 *  Copyright (c) 2017, 2020 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui.preferences;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.cli.install.BootInstallUtils;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstallExtension;
import org.springframework.ide.eclipse.boot.core.cli.install.ZippedBootInstall;
import org.springframework.ide.eclipse.boot.util.version.Version;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Dialog for managing Spring Boot CLI extensions
 *
 * @author Alex Boyko
 *
 */
final class ExtensionsDialog extends TitleAreaDialog {

	private static final ExtensionsContentProvider EXTENSIONS_CONTENT_PROVIDER_INSTANCE = new ExtensionsContentProvider();
	private static final String LOADING_LABEL = "Loading...";

	private final IBootInstall install;
	private Button installButton;
	private Button uninstallButton;
	private TreeViewer extensionsList;
	private Stylers stylers;
	private ExtensionItemModel selected;

	private ValueListener<ExtensionItemState> stateListener = (exp, value) -> updateButtons();

	ExtensionsDialog(Shell parentShell, IBootInstall install) {
		super(parentShell);
		this.install = install;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Create OK button only
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		extensionsList = new TreeViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		stylers = new Stylers(extensionsList.getTree().getFont());
		extensionsList.getTree().addDisposeListener(e -> stylers.dispose());
		extensionsList.setLabelProvider(new ExtensionLabelProvider());
		extensionsList.setContentProvider(EXTENSIONS_CONTENT_PROVIDER_INSTANCE);
		extensionsList.getTree().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		extensionsList.addSelectionChangedListener(selectionEvent -> selectionChanged());

		Composite actionsComposite = new Composite(composite, SWT.NULL);
		actionsComposite.setLayout(GridLayoutFactory.fillDefaults().create());
		actionsComposite.setLayoutData(GridDataFactory.fillDefaults().create());

		installButton = new Button(actionsComposite, SWT.PUSH);
		installButton.setText("Install");
		installButton.setLayoutData(GridDataFactory.fillDefaults().create());
		installButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selected != null) {
					selected.install();
				}
			}
		});

		uninstallButton = new Button(actionsComposite, SWT.PUSH);
		uninstallButton.setText("Uninstall");
		uninstallButton.setLayoutData(GridDataFactory.fillDefaults().create());
		uninstallButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selected != null) {
					selected.uninstall();
				}
			}
		});

		GridData gridData = GridDataFactory.copyData((GridData) actionsComposite.getLayoutData());
		gridData.widthHint = actionsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		actionsComposite.setLayoutData(gridData);

		updateButtons();

		setTitle("Spring Boot CLI Extensions");

		if (install instanceof ZippedBootInstall) {
			setMessage("Read-only list of extensions installed automatically", IMessageProvider.INFORMATION);
		} else {
			setMessage("Manage extensions for Spring Boot CLI installations");
		}

		loadExtensions();

		return parentComposite;
	}

	private void loadExtensions() {
		extensionsList.setInput(Collections.singleton(LOADING_LABEL));
		extensionsList.getTree().setEnabled(false);
		new Job("Loading Spring Boot CLI Extensions") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<ExtensionItemModel> extensionItems = install.supportedExtensions().stream()
						.map(extensionType -> new ExtensionItemModel(extensionType, BootInstallUtils.EXTENSION_TO_TITLE_MAP.get(extensionType)))
						.collect(Collectors.toList());
				getShell().getDisplay().asyncExec(() -> {
					extensionsList.getTree().setEnabled(!(install instanceof ZippedBootInstall));
					extensionsList.setInput(extensionItems);
					extensionItems.forEach(item -> item.getState().addListener((exp, value) -> {
						extensionsList.update(item, null);
					}));
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private void selectionChanged() {
		if (selected != null) {
			selected.getState().removeListener(stateListener);
		}
		selected = extensionsList.getSelection().isEmpty() ? null : (ExtensionItemModel) ((StructuredSelection) extensionsList.getSelection()).getFirstElement();
		if (selected != null) {
			selected.getState().addListener(stateListener);
		} else {
			updateButtons();
		}
	}

	private void updateButtons() {
		if (selected == null) {
			updateButton(installButton, false, false);
			updateButton(uninstallButton, false, false);
		} else {
			switch (selected.getState().getValue()) {
			case UNINSTALLED:
				updateButton(installButton, true, !(install instanceof ZippedBootInstall));
				updateButton(uninstallButton, false, false);
				break;
			case INSTALLED:
				updateButton(installButton, false, false);
				updateButton(uninstallButton, true, !(install instanceof ZippedBootInstall));
				break;
			case INSTALLING:
				updateButton(installButton, true, false);
				updateButton(uninstallButton, false, false);
				break;
			case UNINSTALLING:
				updateButton(installButton, false, false);
				updateButton(uninstallButton, true, false);
				break;
			case ERROR:
				updateButton(installButton, false, false);
				updateButton(uninstallButton, false, false);
				break;
			}
		}
		installButton.getParent().layout(true);
	}

	private static void updateButton(Button button, boolean visible, boolean enabled) {
		button.setVisible(visible);
		button.setEnabled(enabled);
		GridData gridData = GridDataFactory.copyData((GridData)button.getLayoutData());
		gridData.exclude = !visible;
		button.setLayoutData(gridData);
	}

	private enum ExtensionItemState {
		UNINSTALLED,
		INSTALLING,
		INSTALLED,
		UNINSTALLING,
		ERROR
	}

	private class ExtensionItemModel {

		private Class<? extends IBootInstallExtension> extensionType;
		private IBootInstallExtension extension;
		private Version version;
		private String name;
		private LiveVariable<ExtensionItemState> stateVar;

		ExtensionItemModel(Class<? extends IBootInstallExtension> extensionType, String name) {
			this.extensionType= extensionType;
			this.name = name;
			stateVar = new LiveVariable<>(ExtensionItemState.UNINSTALLED);
			init();
		}

		private void init() {
			extension = install.getExtension(extensionType);
			version = extension == null ? null : extension.getVersion();
			if (extension == null) {
				stateVar.setValue(ExtensionItemState.UNINSTALLED);
			} else if (version == null) {
				stateVar.setValue(ExtensionItemState.ERROR);
			} else {
				stateVar.setValue(ExtensionItemState.INSTALLED);
			}
		}

		void install() {
			if (stateVar.getValue() != ExtensionItemState.UNINSTALLED) {
				throw new IllegalStateException();
			}
			stateVar.setValue(ExtensionItemState.INSTALLING);
			new Job("Installing Spring Boot CLI extension `" + name + "'") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						install.installExtension(extensionType);
						if (getShell() != null && !getShell().isDisposed()) {
							getShell().getDisplay().asyncExec(() -> init());
						}
						return Status.OK_STATUS;
					} catch (Exception e) {
						if (getShell() != null && !getShell().isDisposed()) {
							getShell().getDisplay().asyncExec(() -> init());
						}
						return ExceptionUtil.status(e);
					}
				}
			}.schedule();
		}

		void uninstall() {
			if (stateVar.getValue() != ExtensionItemState.INSTALLED) {
				throw new IllegalStateException();
			}
			stateVar.setValue(ExtensionItemState.UNINSTALLING);
			new Job("Uninstalling Spring Boot CLI extension `" + name + "'") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						install.uninstallExtension(extension);
						if (getShell() != null && !getShell().isDisposed()) {
							getShell().getDisplay().asyncExec(() -> init());
						}
						return Status.OK_STATUS;
					} catch (Exception e) {
						if (getShell() != null && !getShell().isDisposed()) {
							getShell().getDisplay().asyncExec(() -> init());
						}
						return ExceptionUtil.status(e);
					}
				}
			}.schedule();
		}

		LiveExpression<ExtensionItemState> getState() {
			return stateVar;
		}

		StyledString getLabel() {
			StyledString label = new StyledString();
			switch (stateVar.getValue()) {
			case UNINSTALLED:
				label.append(name, stylers.grey());
				break;
			case INSTALLING:
				label.append(name, stylers.italic());
				label.append(" Installing...", stylers.italicColoured(SWT.COLOR_BLUE));
				break;
			case UNINSTALLING:
				label.append(name, stylers.italic());
				if (version != null) {
					label.append(" [" + version.toString() + "]", stylers.italicColoured(SWT.COLOR_DARK_GRAY));
				}
				label.append(" Uninstalling...", stylers.italicColoured(SWT.COLOR_BLUE));
				break;
			case INSTALLED:
				label.append(name, stylers.bold());
				if (version != null) {
					label.append(" [" + version.toString() + "]", stylers.boldColoured(SWT.COLOR_DARK_GRAY));
				}
				break;
			case ERROR:
				label.append(name, stylers.red());
				break;
			default:
				break;
			}
			return label;
		}

	}

	private class ExtensionLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			if (cell.getElement() instanceof ExtensionItemModel) {
				ExtensionItemModel extensionItem = (ExtensionItemModel) cell.getElement();
				StyledString label = extensionItem.getLabel();
				cell.setText(label.getString());
				cell.setStyleRanges(label.getStyleRanges());
			} else if (cell.getElement() == LOADING_LABEL) {
				StyledString label = new StyledString(LOADING_LABEL, stylers.bold());
				cell.setText(LOADING_LABEL);
				cell.setStyleRanges(label.getStyleRanges());
			}
		}

	}

	private static class ExtensionsContentProvider extends ArrayContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return false;
		}

	}

}
