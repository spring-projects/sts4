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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.wizard;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.IFrameworkCommandDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ViewerSearchPart;


/**
 * Page that displays commands to configure and execute. Commands may be added
 * even after the page opens and the table is initially populated. Only one
 * command may be executed at a given time. Some commands with no parameters or
 * with just optional parameters can be completed from this page without going
 * to the next page.
 * @author Nieraj Singh
 * @author Christian Dupuis
 */
public class GenericWizardCommandListPage extends AbstractGenericWizardPage {

	private TableViewer commandTableViewer;
	private Text descriptionControl;

	private IFrameworkCommandDescriptor selectedCommandDescriptor;
	private static final String DESCRIPTION_INDENT = "   ";

	private Set<IFrameworkCommandDescriptor> commandDescriptors;

	public static final String PAGE_MESSAGE = "Select a command from the list of commands below. Only one command can be configured for execution at a time";

	// Use the 'S' letter as the keyboard shortcut
	public static final String SELECT_COMMAND_LABEL = "&Select a command to configure and execute:";

	public GenericWizardCommandListPage(String pageName) {
		super(pageName);
		commandDescriptors = new HashSet<IFrameworkCommandDescriptor>();
		setTitle("Select Command");
	}

	protected int getViewerConfiguration() {
		return SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL;
	}

	protected Composite createPageArea(Composite parent) {
		Composite listSection = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(listSection);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(listSection);

		createCommandTableArea(listSection);
		createDescriptionArea(listSection);
		refreshDescriptionArea();

		return listSection;
	}

	public synchronized boolean containsCommandDescriptor(
			IFrameworkCommandDescriptor command) {
		if (commandDescriptors != null) {
			return commandDescriptors.contains(command);
		}
		return false;
	}

	/**
	 * Returns the command descriptor if it is successfully added. Returns null
	 * if the command was not added (either it is null or it already exists)
	 * 
	 * @param command
	 *           descriptor to add
	 * @return returns command if successfully added, null otherwise
	 */
	public synchronized IFrameworkCommandDescriptor addCommandDescriptor(
			IFrameworkCommandDescriptor command) {
		if (command == null) {
			return null;
		}

		if (!commandDescriptors.contains(command)) {
			commandDescriptors.add(command);
			// If the table is already created and not yet disposed
			// it means it is open so also add it to the table
			if (commandTableViewer != null
					&& !commandTableViewer.getTable().isDisposed()) {
				commandTableViewer.add(command);
			}
			return command;
		}
		return null;
	}

	/**
	 * Set a command in the table viewer, if the command is listed in the table.
	 * True is returned if the command is found in the current table input.
	 * <p/>
	 * If it is not already listed in the viewer, no selection will occur, and
	 * false will be returned
	 * <p/>
	 * If the command is null, all selections in the table are cleared, true is
	 * returned
	 * <p/>
	 * 
	 * @param command
	 *           to select. True if selected or selection cleared, false if no
	 *           selection
	 */
	public synchronized boolean selectCommandInViewer(
			IFrameworkCommandDescriptor command) {
		return selectCommand(command);
	}

	/**
	 * Set a command in the table viewer, if the command is listed in the table.
	 * True is returned if the command is found in the current table input.
	 * <p/>
	 * If it is not already listed in the viewer, no selection will occur, and
	 * false will be returned
	 * <p/>
	 * If the command is null, all selections in the table are cleared, true is
	 * returned
	 * <p/>
	 * Note that the table only supports one INSTANCE of a given command, and
	 * they selection is based on the command name, not any other value or
	 * property of the command
	 * 
	 * @param command
	 *           to select. True if selected or selection cleared, false if no
	 *           selection
	 */
	private boolean selectCommand(IFrameworkCommandDescriptor command) {
		// dont do anything if not contained in the list
		if (command != null && !commandDescriptors.contains(command)) {
			return false;
		}
		selectedCommandDescriptor = command;
		if (commandTableViewer != null) {
			// If null clear everything
			if (command == null) {
				commandTableViewer.getTable().deselectAll();

				// Clear the selections in the wizard as well
				GenericWizardCommandListPage.this.selectionChanged(null);
			} else {
				commandTableViewer.setSelection(
						new StructuredSelection(command), true);
			}
			return true;
		}
		return false;
	}

	protected ViewerSearchPart createSearchControl(Composite parent) {
		ViewerSearchPart part = new ViewerSearchPart(parent) {

			protected boolean matches(Object element, Object parentElement,
					String pattern) {
				if (element instanceof IFrameworkCommandDescriptor) {
					String commandName = ((IFrameworkCommandDescriptor) element)
							.getName();
					String lowerCasePattern = pattern.toLowerCase();

					if (commandName != null) {
						commandName = commandName.toLowerCase();
						SearchPattern filter = new SearchPattern();
						filter.setPattern(lowerCasePattern);
						return filter.matches(commandName);
					}
				}
				return false;
			}
		};
		return part;
	}

	protected synchronized void createCommandTableArea(Composite parent) {
		Composite tableArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(tableArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableArea);

		Label tableLabel = new Label(tableArea, SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(tableLabel);

		tableLabel.setText(SELECT_COMMAND_LABEL);

		final ViewerSearchPart part = createSearchControl(tableArea);

		Table table = new Table(tableArea, getViewerConfiguration());

		commandTableViewer = new TableViewer(table);

		part.connectViewer(commandTableViewer);

		setTableColumnAndLayout(commandTableViewer);

		commandTableViewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection<?>) {
					Collection<?> topLevel = (Collection<?>) inputElement;
					return topLevel.toArray();
				}
				return null;
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Nothing
			}

			public void dispose() {
				// Nothing
			}
		});

		commandTableViewer.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				if (element instanceof IFrameworkCommandDescriptor) {
					return ((IFrameworkCommandDescriptor) element).getName();
				}
				return null;
			}

		});

		commandTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) commandTableViewer
								.getSelection();
						if (selection != null) {
							Object selObj = selection.getFirstElement();
							if (selObj instanceof IFrameworkCommandDescriptor) {
								GenericWizardCommandListPage.this
										.selectionChanged((IFrameworkCommandDescriptor) selObj);
							}
						}
						checkPageComplete();
					}
				});

		commandTableViewer.getTable().addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {

			}

			public void keyPressed(KeyEvent e) {
				// STS-1250: Add support to navigate to search control when up
				// arrow pressed
				if (e.keyCode == SWT.ARROW_UP) {
					Object topSelection = commandTableViewer.getElementAt(0);
					IFrameworkCommandDescriptor currentSelection = getSelectedCommandDescriptor();
					if (topSelection == currentSelection) {
						part.getTextControl().setFocus();
					}
				}
			}
		});

		commandTableViewer.setInput(commandDescriptors);

		Text searchText = part.getTextControl();

		searchText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {

			}

			public void keyPressed(KeyEvent e) {
				// STS-1250: Add support to navigate to table viewer when
				// down arrow is pressed
				if (e.keyCode == SWT.ARROW_DOWN) {
					commandTableViewer.getTable().setFocus();
					Object selection = commandTableViewer.getElementAt(0);
					if (selection instanceof IFrameworkCommandDescriptor) {
						selectCommandInViewer((IFrameworkCommandDescriptor) selection);
					}
				}
			}
		});

		// STS 1251: Add filter listener to automatically select commands based
		// on the pattern that is typed
		searchText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent event) {
				// STS 1251: If one element is left in the table after search is
				// performed, select it to enable
				// the appropriate wizard buttons
				int itemCount = commandTableViewer.getTable().getItemCount();
				if (itemCount == 1) {
					IFrameworkCommandDescriptor remainingDescriptor = (IFrameworkCommandDescriptor) commandTableViewer
							.getElementAt(0);
					if (remainingDescriptor != null) {
						GenericWizardCommandListPage.this
								.selectCommandInViewer(remainingDescriptor);
					}
				} else {
					// else clear the selection nothing is found by search or
					// multiple matches are found
					GenericWizardCommandListPage.this
							.selectCommandInViewer(null);
				}

			}

			public void keyPressed(KeyEvent event) {

			}
		});

		searchText.setFocus();
		checkPageComplete();
	}

	/**
	 * Uses can override to modify the behaviour of the wizard when a selection
	 * changes. Note that can intercept a selection change and prevent the
	 * default behaviour of assigning the selection to the wizard. This allows
	 * users to perform additional checks before delegating to the super
	 * selectChange implementation
	 * 
	 * @param selectedDescriptor
	 */
	protected void selectionChanged(
			IFrameworkCommandDescriptor selectedDescriptor) {
		selectedCommandDescriptor = selectedDescriptor;
		getGenericCommandWizard().setCommandInstance(selectedCommandDescriptor);
		refreshDescriptionArea();
		checkPageComplete();
	}

	protected int getMinTableHeight() {
		return 200;
	}

	protected int getMinCommandColumnWeight() {
		return 400;
	}

	/**
	 * Sets the main column in the viewer that lists all the commands and adds a
	 * sorter.
	 * 
	 * @param tableviewer
	 */
	protected void setTableColumnAndLayout(final TableViewer tableviewer) {
		tableviewer.setSorter(new ViewerSorter() {

			public int compare(Viewer viewer, Object command1, Object command2) {
				if (viewer instanceof TableViewer) {
					Table table = ((TableViewer) viewer).getTable();

					if (command1 instanceof IFrameworkCommandDescriptor
							&& command2 instanceof IFrameworkCommandDescriptor) {
						int sortDirection = table.getSortDirection();
						String commandName1 = ((IFrameworkCommandDescriptor) command1)
								.getName();
						String commandName2 = ((IFrameworkCommandDescriptor) command2)
								.getName();
						return sortDirection == SWT.UP ? commandName1
								.compareToIgnoreCase(commandName2)
								: commandName2
										.compareToIgnoreCase(commandName1);
					}
				}
				return super.compare(viewer, command1, command2);
			}

		});

		final Table table = tableviewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true)
				.hint(SWT.DEFAULT, getMinTableHeight()).applyTo(table);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(
				getMinCommandColumnWeight(), true));

		TableColumn commandColumn = new TableColumn(table, SWT.NONE);
		table.setLayout(tableLayout);
		// the command column cannot be null, as the table viewer must have at
		// least one column
		commandColumn.setText("Commands");
		commandColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				int dir = table.getSortDirection();
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				table.setSortDirection(dir);
				tableviewer.refresh();
			}
		});
		table.setSortColumn(commandColumn);
		table.setSortDirection(SWT.UP);
		// Hide the header for now. If sorting direction change is required,
		// simply set to true:
		table.setHeaderVisible(false);
		table.layout(true);
		tableviewer.refresh();
	}

	protected void checkPageComplete() {
		IFrameworkCommandDescriptor command = getSelectedCommandDescriptor();
		if (command == null || hasRequiredParameters(command)) {
			setPageComplete(false);

		} else {
			setPageComplete(true);
		}
	}

	public boolean isPageComplete() {
		// Dont block termination of wizard if
		// it is not the current page
		if (!isCurrentPage()) {
			return true;
		}
		return super.isPageComplete();
	}

	public boolean canFlipToNextPage() {
		IFrameworkCommandDescriptor command = getSelectedCommandDescriptor();
		if (command == null) {
			setMessage(PAGE_MESSAGE);
			return false;
		} else if (hasRequiredParameters(command)) {
			setMessage("This command has required parameters with values to set. Click next to set the values.");
			return true;
		} else if (command.getParameters().length == 0) {
			setMessage("This command has no parameters to set. Click finish to execute the command.");
			return false;
		} else {
			setMessage("Click next to set optional parameter values, or click finish to execute the command.");
			return true;
		}
	}

	protected boolean hasRequiredParameters(
			IFrameworkCommandDescriptor commandDescriptor) {
		ICommandParameterDescriptor[] parameters = commandDescriptor
				.getParameters();
		for (ICommandParameterDescriptor parameter : parameters) {
			if (parameter.isMandatory()) {
				return true;
			}
		}
		return false;
	}

	protected void createDescriptionArea(Composite parent) {

		Composite descriptionArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(descriptionArea);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(descriptionArea);

		Label descriptionLabel = new Label(descriptionArea, SWT.LEFT);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(descriptionLabel);
		descriptionLabel.setText("Command Description:");

		descriptionControl = new Text(descriptionArea, SWT.V_SCROLL
				| SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);

		GridDataFactory.fillDefaults().grab(true, true)
				.hint(getDescriptionWidthHint(), getDescriptionHeightHint())
				.applyTo(descriptionControl);
	}

	protected int getDescriptionWidthHint() {
		return SWT.DEFAULT;
	}

	protected int getDescriptionHeightHint() {
		return 180;
	}

	public synchronized IFrameworkCommandDescriptor getSelectedCommandDescriptor() {
		return selectedCommandDescriptor;
	}

	protected void refreshDescriptionArea() {

		if (descriptionControl != null && !descriptionControl.isDisposed()) {
			DescriptionSetter buffer = new DescriptionSetter(
					getSelectedCommandDescriptor(), descriptionControl);
			buffer.setDescription();
		}

	}

	protected static class DescriptionSetter {

		private int lines = 0;
		private IFrameworkCommandDescriptor command;
		private Text descriptionText;
		private StringBuffer buffer;

		public DescriptionSetter(IFrameworkCommandDescriptor command,
				Text descriptionText) {
			this.command = command;
			this.descriptionText = descriptionText;

		}

		public String setDescription() {
			buffer = new StringBuffer();
			if (command == null) {
				appendLine("n/a");
			} else {
				String description = command.getDescription();
				if (description != null && description.length() > 0) {
					appendLine(description);

				} else {
					appendLine("Description not available");
				}

				ICommandParameterDescriptor[] parameters = command
						.getParameters();
				appendLine("");
				appendLine("Parameters:");
				if (parameters == null || parameters.length == 0) {
					appendLine(DESCRIPTION_INDENT
							+ "This command has no parameters");
				} else {

					for (ICommandParameterDescriptor param : parameters) {
						StringBuffer params = new StringBuffer();
						params.append(DESCRIPTION_INDENT);
						params.append(param.getName());
						params.append(" -- ");
						params.append(param.isMandatory() ? " is required"
								: " optional");
						appendLine(params.toString());
					}

				}
			}
			String text = buffer.toString();
			descriptionText.setText(text);
			return text;
		}

		public int getLines() {
			return lines;
		}

		protected void appendLine(String text) {
			buffer.append(text);
			buffer.append(System.getProperty("line.separator"));
			lines++;
		}
	}

}
