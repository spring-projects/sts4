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

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.SearchPattern;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.BasePluginData;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.icons.IconManager;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.IProjectSelectionHandler;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.IProjectSelector;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ProjectSelectionPart;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ProjectSelectorFactory;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.SWTFactory;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ViewerSearchPart;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.tasks.IUIRunnable;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.tasks.TaskManager;


/**
 * The manager allows plugins to be selected for install, uninstall or update
 * <p>
 * The actual operations on the selected plugins are executed only after the
 * dialogue closes.
 * </p>
 * <p>
 * The install and uninstall operations can be used to undo each other. So if a
 * plugin is marked as selected for install, pressing the uninstall operation
 * undos the selection, restoring the plugin state to its original state.
 * </p>
 * <p>
 * IMPORTANT: to understand the behaviour of this manager, it is important to
 * distinguish between the underlying plugin model and the tree elements in a
 * viewer that represent the plugin model. They are separate, although a tree
 * element always holds a reference to its corresponding plugin model entity.
 * </p>
 * <p>
 * When the manager first opens it will populate the list of plugins based on
 * the local list of available plugins as well as will mark those plugins on
 * that list that are installed in a given project context. This list can be
 * refreshed by requesting Grails for an updated list, or it can be reset to the
 * state the list was in when the manager first opened (i.e. the state based on
 * the local list of plugins).
 * </p>
 * Plugins and their versions are represented in a tree viewer as tree elements
 * that hold a reference to a plugin version model entity. In addition, these
 * tree elements also hold selection and install state, indicating whether the
 * plugin is currently installed, or the selection status while the manager is
 * still open (i.e. whether a plugin has been selected for upgrade, uninstall,
 * etc..
 * <p>
 * A tree element is therefore the UI representation of a plugin version model
 * entity, with additional information specific to the operations that can be
 * performed with this manager.
 * </p>
 * <p>
 * The tree viewer itself displays two types of elements, a root element and a
 * version element. BOTH of these represent a plugin version model entity,
 * except that the root element indicates which version is either available for
 * install OR which version is currently installed. The version element simply
 * indicates a particular version model entity for a plugin. The list of child
 * version elements is IMMUTABLE, but the root element version is NOT. The
 * reason is that the root element always reflects which version of a plugin the
 * user has selected for a particular operation, and therefore may change as the
 * user changes selections, and performs different operations.
 * </p>
 * @author Nieraj Singh
 * @author Andy Clement
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 * @author Kris De Volder
 */
public abstract class PluginManagerDialog extends CommandDialog {

	public PluginManagerDialog(Shell parentShell,
			List<IProject> projects) {
		super(parentShell, projects);
	}

	public static final String INITIAL_REFRESH_DEPENDENCIES_DIALOGUE_TITLE = "No local plugin list found";

	private GrailsBrowserViewer pluginViewer;

	private IProject selectedProject;

	private Text descriptionLabel;
	private Label authorLabel;
	private Link documentationLink;

	private IconManager iconManager;

	private static final int MIN_DESCRIPTION_HEIGHT = 65;
	private static final int MIN_DESCRIPTION_WIDTH = SWT.DEFAULT;

	/**
	 * Read/write only through appropriate getter method. Do not access directly
	 */
	private Map<Plugin, RootTreeElement> rootElementMap;

	private Map<PluginVersion, VersionTreeElement> childElementMap;

	/**
	 * Local cache of all the plugins. Used for resetting only.
	 */
	private Collection<? extends Plugin> pluginList;

	private static final String DATA_NOT_AVAILABLE = "n/a";

	private LinkedHashSet<PluginVersion> selectedToInstall = new LinkedHashSet<PluginVersion>();
	private LinkedHashSet<PluginVersion> selectedToUninstall = new LinkedHashSet<PluginVersion>();

	private List<Button> selectionBasedOperationButtons;

	private ViewerSearchPart searchPart;

	public enum PluginColumnType implements IPluginListColumn {

		PLUGIN_STATE("", 12), PLUGIN_NAME("Name", 20), TITLE("Title", 80), VERSION(
				"Version", 10);

		private String name;
		private int weight;

		private PluginColumnType(String name, int weight) {
			this.name = name;
			this.weight = weight;
		}

		public String getName() {
			return name;
		}

		public int getWidth() {
			return weight;
		}

	}

	/**
	 * Plugins can be filtered based on theis state (if installed, selected for
	 * change, or having updates)
	 * 
	 * @author nisingh
	 * 
	 */
	public enum GrailsFilterType {

		SHOW_ALL("Show all", true), SHOW_INSTALLED_ONLY("Show installed only",
				false), SHOW_AVAILABLE_UPDATES_ONLY(
				"Show available updates only", false), SHOW_CHANGES_ONLY(
				"Show pending changes only", false);
		private String filterName;
		private boolean defaultSelection;

		private GrailsFilterType(String filterName, boolean defaultSelection) {
			this.filterName = filterName;
			this.defaultSelection = defaultSelection;
		}

		public String getFilterName() {
			return filterName;
		}

		public boolean getDefaultSelection() {
			return defaultSelection;
		}
	}

	/**
	 * Search area that allows users to filter the list of plugins based on text
	 * patterns.
	 * 
	 * @param parent
	 */
	protected void createSearchArea(Composite parent) {

		searchPart = new ViewerSearchPart(parent) {

			protected boolean matches(Object element, Object parentElement,
					String pattern) {

				if (element instanceof ITreeElement) {
					ITreeElement treeElement = (ITreeElement) element;
					PluginVersion version = treeElement.getVersionModel();
					if (PluginManagerDialog.this.matches(version,
							pattern)) {
						return true;
					} else {
						List<PluginVersion> versions = version.getParent()
								.getVersions();
						if (versions != null) {
							for (PluginVersion ver : versions) {
								if (PluginManagerDialog.this.matches(
										ver, pattern)) {
									return true;
								}
							}
						}
					}
				}
				return false;
			}
		};

	}

	protected void initIconManager() {
		iconManager = new IconManager();
	}

	/**
	 * Determines if data contained in the plugin model element matches a given
	 * pattern. This data could be the model element name, title, or
	 * description. True if it matches, false otherwise
	 * 
	 * @param version
	 *           plugin model element whose data needs to be compared against a
	 *           pattern
	 * @param pattern
	 * @return true if plugin model element data matches the given pattern.
	 */
	protected boolean matches(PluginVersion version, String pattern) {
		if (version == null || pattern == null) {
			return false;
		}
		String lowerCasePattern = pattern.toLowerCase();
		String name = version.getName();
		SearchPattern filter = new SearchPattern();
		filter.setPattern(lowerCasePattern);

		if (name != null && filter.matches(name)) {
			return true;
		}

		String title = version.getTitle();
		if (title != null && filter.matches(title)) {
			return true;
		}
		return false;
	}

	/**
	 * The main UI area of the dialogue containing all the controls of the
	 * manager
	 */
	protected void createCommandArea(Composite parent) {
		createProjectArea(parent);

		createPluginViewerArea(parent);

		// Populate the viewer for the first time
		resynchPluginsList(false);

		pluginViewer.getTreeViewer().getTree().setFocus();
	}

	/**
	 * The project selection area.
	 * 
	 * @param parent
	 */
	protected void createProjectArea(Composite parent) {

		// Set the default selection based on the first selected project
		Collection<IProject> selectedProjects = getProjects();

		LinkedHashSet<IProject> availableProjects = new LinkedHashSet<IProject>();

		if (selectedProjects != null && !selectedProjects.isEmpty()) {
			availableProjects.addAll(selectedProjects);
		}

		// Get all workspace projects
		Collection<IProject> allProjects = updateProjects();
		if (!allProjects.isEmpty()) {
			availableProjects.addAll(allProjects);
		}

		IProjectSelectionHandler handler = new IProjectSelectionHandler() {

			public void handleProjectSelectionChange(IProject project) {
				PluginManagerDialog.this.selectedProject = project;
				resynchPluginsList(false);
			}
		};

		IProjectSelector selector = new ProjectSelectorFactory(getShell(),
				parent, availableProjects, handler).getProjectSelector();

		if (selector != null) {
			selector.createProjectArea();
			if (selector instanceof ProjectSelectionPart) {
				((ProjectSelectionPart) selector)
						.showProjectSwitchDialogue(true);
			}
			// Initial selection
			if (!availableProjects.isEmpty()) {
				selectedProject = availableProjects.iterator().next();
				selector.setProject(selectedProject);
			}
		}

	}

	protected abstract Collection<IProject> updateProjects();

	protected void clearAllPluginChanges() {
		if (selectedToInstall != null) {
			selectedToInstall.clear();
		}
		if (selectedToUninstall != null) {
			selectedToUninstall.clear();
		}
	}

	public boolean hasPluginChanges() {
		return (selectedToInstall != null && !selectedToInstall.isEmpty())
				|| (selectedToUninstall != null && !selectedToUninstall
						.isEmpty());
	}

	protected void createFilterOptionsArea(Composite parent) {

		Group optionsGroup = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsGroup);
		GridLayoutFactory
				.fillDefaults()
				.numColumns(1)
				.margins(getDefaultCompositeHMargin(),
						getDefaultCompositeVMargin()).applyTo(optionsGroup);
		optionsGroup.setText("Filters");

		Composite filterOptionsArea = new Composite(optionsGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2)
				.applyTo(filterOptionsArea);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(filterOptionsArea);

		if (optionsGroup != null) {
			GrailsFilterType[] types = GrailsFilterType.values();

			for (GrailsFilterType type : types) {
				String filterName = type.getFilterName();
				if (filterName != null) {
					Button button = SWTFactory.createRadialButton(
							filterOptionsArea, filterName,
							type.getDefaultSelection());
					if (button != null) {
						button.addSelectionListener(new ViewerFilterButtonListener());
						button.setData(type);
					}
				}
			}
		}
	}

	protected void createDescriptionArea(Composite parent) {

		Composite descriptionArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(descriptionArea);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(descriptionArea);

		Group descriptionGroup = new Group(descriptionArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(descriptionGroup);
		GridLayoutFactory
				.fillDefaults()
				.numColumns(1)
				.margins(getDefaultCompositeHMargin(),
						getDefaultCompositeVMargin()).applyTo(descriptionGroup);
		descriptionGroup.setText("Published Plugin Information");

		Label desLabel = new Label(descriptionGroup, SWT.LEFT);
		desLabel.setText("Description:");

		descriptionLabel = new Text(descriptionGroup, SWT.V_SCROLL | SWT.BORDER
				| SWT.READ_ONLY | SWT.WRAP);

		GridDataFactory.fillDefaults().grab(true, true)
				.hint(MIN_DESCRIPTION_WIDTH, MIN_DESCRIPTION_HEIGHT)
				.applyTo(descriptionLabel);

		Composite authorArea = new Composite(descriptionGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(authorArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(authorArea);

		Label authLab = new Label(authorArea, SWT.LEFT);
		authLab.setText("Author:");

		authorLabel = new Label(authorArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(authorLabel);

		Composite linkArea = new Composite(descriptionGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(linkArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(linkArea);

		Label descLinkLabel = new Label(linkArea, SWT.LEFT);
		descLinkLabel.setText("Documentation:");

		documentationLink = new Link(linkArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(documentationLink);

		documentationLink.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Object dataObj = documentationLink.getData();
				if (dataObj instanceof BasePluginData) {
					String urlExpression = ((BasePluginData) dataObj)
							.getDocumentation();
					handleNavigation(urlExpression);
				}
			}

		});
		refreshDescriptionArea();

	}

	/**
	 * Handles navigation from a hyperlink in the description area.
	 * 
	 * @param urlExpression
	 */
	protected void handleNavigation(String urlExpression) {

		String errorMessage = null;

		try {
			URL url = new URL(urlExpression);
			boolean launched = Program.launch(url.toString());
			if (!launched) {
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
						.getBrowserSupport();
				try {
					if (support != null) {
						IWebBrowser browser = support.getExternalBrowser();
						if (browser != null) {
							browser.openURL(url);
							return;
						} else {
							errorMessage = "Unable to find browser support to navigate to URL. Check default browser support in Eclipse or OS";
						}
					}
				} catch (PartInitException e) {
					errorMessage = "Unable to navigate to URL: "
							+ e.getLocalizedMessage();
				}
			}
		} catch (MalformedURLException e) {
			errorMessage = "Unable to navigate to URL: "
					+ e.getLocalizedMessage();
		}

		if (errorMessage != null) {

			InternalMessageDialogue dialog = new InternalMessageDialogue(
					"Problems navigating to URL", errorMessage + ": "
							+ urlExpression, MessageDialog.ERROR, false);
			dialog.open();
		}

	}

	/**
	 * This gets invokes any time a selection changes in the tree viewer.
	 * Updates the description controls with the selected plugin information. If
	 * more than one plugin is selected, a "n/a" is displayed for each
	 * description field.
	 */
	protected void refreshDescriptionArea() {

		PluginVersion version = getSingleSelectedPluginVersion();

		if (version != null) {
			String description = version.getDescription();
			if (description != null && description.length() > 0) {
				descriptionLabel.setText(description);
			} else {
				descriptionLabel.setText(DATA_NOT_AVAILABLE);
			}

			String author = version.getAuthor();
			if (author != null && author.length() > 0) {
				authorLabel.setText(author);
			} else {
				authorLabel.setText(DATA_NOT_AVAILABLE);
			}

			String documentation = version.getDocumentation();
			if (documentation != null && documentation.length() > 0) {
				documentation = "<a href=\"" + documentation + "\">"
						+ documentation + "</a>";
				documentationLink.setText(documentation);
			} else {
				documentationLink.setText(DATA_NOT_AVAILABLE);
			}
			documentationLink.setData(version);
			return;
		}

		descriptionLabel.setText(DATA_NOT_AVAILABLE);
		authorLabel.setText(DATA_NOT_AVAILABLE);
		documentationLink.setText(DATA_NOT_AVAILABLE);
		documentationLink.setData(null);

	}

	/**
	 * Will return a selected Plugin IF and ONLY IF there is one selection.
	 * Returns null if there are no plugins selected or multiple plugins
	 * selected
	 * 
	 * @return one selected plugin, or null in any other case
	 */
	protected PluginVersion getSingleSelectedPluginVersion() {
		IStructuredSelection selection = (IStructuredSelection) pluginViewer
				.getTreeViewer().getSelection();
		if (selection != null && selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (element instanceof ITreeElement) {
				return ((ITreeElement) element).getVersionModel();
			}
		}
		return null;
	}

	/**
	 * Creates the main tree viewer area that contains the tree viewer that
	 * displays the list of plugins as well as the button selection controls
	 * 
	 * @param parent
	 */
	protected void createPluginViewerArea(Composite parent) {

		createSearchArea(parent);

		Composite viewerArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false)
				.applyTo(viewerArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewerArea);

		Composite treeArea = new Composite(viewerArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(treeArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeArea);

		pluginViewer = new GrailsBrowserViewer(getColumns());
		pluginViewer.createControls(treeArea);

		TreeViewer viewer = pluginViewer.getTreeViewer();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				refreshDescriptionArea();
				validateOperationButtons();
			}
		});

		viewer.addFilter(new PluginStatusFilter());
		viewer.setContentProvider(new GrailsViewerContentProvider());
		viewer.setLabelProvider(new GrailsViewerLabelProvider());
		viewer.setSorter(new GrailsViewerColumnSorter());

		createDescriptionArea(treeArea);

		createFilterOptionsArea(treeArea);

		createOperationButtonArea(viewerArea);

		searchPart.connectViewer(pluginViewer.getTreeViewer());

		initIconManager();
	}

	/**
	 * Determine which selection based operation buttons to enable/disable based
	 * on the current plugin selection
	 */
	protected void validateOperationButtons() {
		// Determine which operation buttons to enable based on the
		// current selection
		List<ITreeElement> selectedElements = getSelectedTreeElements();
		List<Button> operationButtons = getPluginOperationButtons();

		boolean disableAll = selectedElements.isEmpty();

		for (Button button : operationButtons) {

			if (disableAll) {
				button.setEnabled(false);
			} else {
				// Allow the selection to determine if the operation associated
				// with the button should enable the button
				for (ITreeElement element : selectedElements) {
					// In place always disable all buttons
/*					if (element.getVersionModel().getParent().isInPlace()) {
						button.setEnabled(false);
						break;
					} else */ if (!enableOperationButtonState(button, element)) {
						// If at least one element cannot handle the given
						// operation
						// the button is disabled and there is no further need
						// to
						// check the remaining elements.
						break;
					}
				}
			}
		}
	}

	/**
	 * Change the state of an operation button, based on the state of a given
	 * tree element If the button is not a selection-based operation button, no
	 * changes are peformed and the current state of the button is retained and
	 * returned.
	 * 
	 * @param button
	 *           to change state, IF it is a selection-based operation button
	 * @param element
	 *           to test if button enable state needs to change
	 * @return new enable state of the button, or the original state if the
	 *        button state is not changed
	 */
	protected boolean enableOperationButtonState(Button button,
			ITreeElement element) {
		Object data = button.getData();
		boolean isEnabled = button.getEnabled();
		if (data instanceof PluginOperation) {
			PluginOperation operation = (PluginOperation) data;
			isEnabled = element.isValidOperation(operation);
			button.setEnabled(isEnabled);
		}
		return isEnabled;
	}

	/**
	 * Creates a button for each available plugin operation.
	 * 
	 * @param parent
	 */
	protected void createOperationButtonArea(Composite parent) {

		Composite buttons = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults()
				.align(GridData.CENTER, GridData.BEGINNING).applyTo(buttons);

		GridLayoutFactory.fillDefaults().applyTo(buttons);

		PluginOperation[] types = PluginOperation.values();
		selectionBasedOperationButtons = new ArrayList<Button>(types.length);
		for (PluginOperation type : types) {
			Button button = createSelectionButton(buttons, type);
			if (button != null && isSelectionBasedOperation(type)) {
				selectionBasedOperationButtons.add(button);
			}
		}
	}

	protected boolean isSelectionBasedOperation(PluginOperation operation) {
		if (operation == null) {
			return false;
		}

		switch (operation) {
		case INSTALL:
		case UNINSTALL:
		case UPDATE:
			return true;
		}
		return false;
	}

	/**
	 * Buttons that operate on selections in the tree viewer. May be null if no
	 * buttons have been initiliased.
	 * 
	 * @return list of buttons or null if not initialised
	 */
	protected List<Button> getPluginOperationButtons() {
		return selectionBasedOperationButtons;
	}

	/**
	 * Creates a button and adds appropriate listeners for a given operation
	 * 
	 * @param parent
	 * @param type
	 * @return
	 */
	protected Button createSelectionButton(Composite parent,
			PluginOperation type) {
		if (type == null) {
			return null;
		}

		Button button = new Button(parent, SWT.PUSH);
		button.setText(type.getName());

		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

		GridDataFactory.fillDefaults()
				.hint(Math.max(widthHint, minSize.x), SWT.DEFAULT)
				.applyTo(button);

		button.setData(type);
		button.addSelectionListener(new PluginSelectionButtonListener());
		return button;
	}

	protected PluginColumnType getColumnType(String name) {
		if (name == null) {
			return null;
		}

		PluginColumnType[] types = PluginColumnType.values();
		for (PluginColumnType type : types) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	protected IPluginListColumn[] getColumns() {

		PluginColumnType[] type = PluginColumnType.values();

		IPluginListColumn[] columns = new IPluginListColumn[type.length];

		for (int i = 0; i < columns.length && i < type.length; i++) {
			columns[i] = type[i];
		}

		return columns;
	}

	/**
	 * Resynch the plugin list. If the 'aggressive' flag is set, then a more forcefull refresh 
	 * will be performed, clearing both in-memory and on-disk caches. The agressive flag will
	 * be set, typically, when the resynch is triggered by the user explicitly clicking on the
	 * refresh button. In other cases it won't typically be set.
	 */
	protected void resynchPluginsList(final boolean aggressive) {
		IUIRunnable pluginListCommand = new IUIRunnable() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				pluginList = updatePlugins(aggressive, monitor);
			}

			public Shell getShell() {
				return PluginManagerDialog.this.getShell();
			}

		};
		TaskManager.runSynch(pluginListCommand,
				"Resolving list of available plugins.");

		setViewerInput();
	}

	/**
	 * If the 'aggressive' flag is set, then a more forceful refresh 
	 * will be performed, clearing both in-memory and on-disk caches. The aggressive flag will
	 * be set, typically, only when triggered by the user explicitly clicking on the
	 * refresh button.
	 */
	protected abstract Collection<? extends Plugin> updatePlugins(boolean aggressive, IProgressMonitor monitor);


	/**
	 * Given a plugin model, returns the corresponding root tree viewer element
	 * <p>
	 * if a root tree viewer element cannot be found, it will attempt to create
	 * one and return it
	 * </p>
	 * Return null if it cannot create a root element, or it cannot find an
	 * existing root element
	 * 
	 * @param plugin
	 * @return
	 */
	protected RootTreeElement getRootElement(Plugin plugin) {
		if (plugin == null || rootElementMap == null) {
			return null;
		}
		RootTreeElement element = rootElementMap.get(plugin);

		if (element == null) {
			element = new RootTreeElement(plugin.getLatestReleasedVersion());
			rootElementMap.put(plugin, element);
		}

		return element;
	}

	/**
	 * Returns a child version tree element for the corresponding plugin version
	 * model.
	 * 
	 * @param version
	 * @return
	 */
	protected VersionTreeElement getVersionElement(PluginVersion version) {
		if (version == null || childElementMap == null) {
			return null;
		}
		VersionTreeElement element = childElementMap.get(version);

		if (element == null) {

			// No orphan children. Add only if a parent is found.
			// Must have a Parent
			RootTreeElement rootElement = getRootElement(version.getParent());
			if (rootElement != null) {
				element = new VersionTreeElement(version, rootElement);
				childElementMap.put(version, element);
			} else {
				FrameworkUIActivator.log(new Status(IStatus.ERROR, FrameworkUIActivator.PLUGIN_ID, 						
								"Orphaned version: "
										+ version.getName()
										+ " "
										+ version.getVersion()
										+ ". Cannot find a published plugin. Omitting from manager plugin's list"
								));
			}
		}

		return element;
	}

	/**
	 * Get the plugins that are selected for install. It is never null, but can
	 * be empty.
	 * 
	 * @return non-null list of selected plugins for install. Can be empty.
	 */
	public Collection<PluginVersion> getSelectedToInstall() {
		return selectedToInstall;
	}

	/**
	 * Get the plugins that are selected for uninstall. It is never null, but
	 * can be empty.
	 * 
	 * @return non-null list of selected plugins for install. Can be empty.
	 */
	public Collection<PluginVersion> getSelectedtoUninstall() {
		return selectedToUninstall;
	}

	/**
	 * Get currently selected project for which plugin management should be
	 * performed. Should never be null.
	 * 
	 * @return non-null project for which plugin management should be performed.
	 */
	public IProject getSelectedProject() {
		return selectedProject;
	}

	/**
	 * Given a root element, it will either UNDO a select-install operation OR
	 * mark the root element for Uninstall. The root element's corresponding
	 * child version element will also be updated.
	 * 
	 * @param root
	 */
	protected void uninstallRoot(RootTreeElement root) {
		if (root == null) {
			return;
		}
		PluginState currentState = root.getPluginState();
		if (currentState == PluginState.SELECT_INSTALL) {
			// Restore the original state.
			restoreOriginalState(root);
		} else if (currentState == PluginState.INSTALLED
				|| currentState == PluginState.UPDATE_AVAILABLE) {
			// select corresponding version and mark both the root and version
			// for
			// uninstall
			VersionTreeElement associatedVersion = root
					.getSelectedVersionTreeElement();
			uninstallVersion(associatedVersion);
		}
	}

	/**
	 * Marks the given version state and its corresponding root to Install, but
	 * does NOT validate to make sure it is possible to mark it to install.
	 * Callers must validate before calling this method. When marked for
	 * install, all other version states are cleared for that plugin.
	 * 
	 * @param selectedVersionElement
	 */
	protected void installVersion(VersionTreeElement selectedVersionElement) {
		if (selectedVersionElement == null) {
			return;
		}
		RootTreeElement root = selectedVersionElement.getParent();

		if (root != null) {
			restoreOriginalState(root);
			selectedVersionElement.setState(PluginState.SELECT_INSTALL);
			addToInstallSelection(selectedVersionElement.getVersionModel());
			root.setSelectedVersionTreeElement(selectedVersionElement);
			root.setState(PluginState.SELECT_INSTALL);
		}
	}

	/**
	 * Marks the given version state and its corresponding root state to
	 * Uninstall. Note that this does not check for validity. It simply clears
	 * state for all other versions of the plugin, and marks the specified
	 * version and its corresponding root with an Uninstall state.
	 * 
	 * @param selectedVersionElement
	 *           which needs to be set to "Uninstall" along with its root.
	 */
	protected void uninstallVersion(VersionTreeElement selectedVersionElement) {
		if (selectedVersionElement == null) {
			return;
		}

		RootTreeElement root = selectedVersionElement.getParent();
		if (root != null) {
			restoreOriginalState(root);
			selectedVersionElement.setState(PluginState.SELECT_UNINSTALL);
			addToUninstallSelection(selectedVersionElement.getVersionModel());
			root.setSelectedVersionTreeElement(selectedVersionElement);
			root.setState(PluginState.SELECT_UNINSTALL);
		}
	}

	/**
	 * Plugin models added to this list get installed after the wizard
	 * completes. THis list also includes plugin models selected for upgrade.
	 * This may change if Grails adds a distinct "upgrade" plugin command. As of
	 * 1.3.4, the "install-plugin" handles both cases.
	 * 
	 * @param version
	 */
	protected void addToInstallSelection(PluginVersion version) {
		if (version != null && !selectedToInstall.contains(version)) {
			selectedToInstall.add(version);
			removeFromUninstallSelection(version);
		}
	}

	/**
	 * Plugin models added to this list get uninstalled after the wizard
	 * completes.
	 * 
	 * @param version
	 */
	protected void addToUninstallSelection(PluginVersion version) {
		if (version != null && !selectedToUninstall.contains(version)) {
			selectedToUninstall.add(version);
			removeFromInstallSelection(version);
		}
	}

	protected void removeFromInstallSelection(PluginVersion version) {
		selectedToInstall.remove(version);
	}

	protected void removeFromUninstallSelection(PluginVersion version) {
		selectedToUninstall.remove(version);
	}

	/**
	 * Restores a root element to its pre-selection state.
	 * <p>
	 * For plugins that are not installed but marked for install, this means
	 * clearing the plugin state (no state) For plugins that are installed but
	 * marked for uninstall, this means restoring the plugin state to that of
	 * installed (Or update available).
	 * </p>
	 * 
	 * @param element
	 */
	protected void restoreOriginalState(RootTreeElement element) {
		if (element == null) {
			return;
		}

		// Clear the current selection first
		VersionTreeElement currentSelection = element
				.getSelectedVersionTreeElement();
		if (currentSelection != null) {
			PluginVersion currentSelectionVersion = currentSelection
					.getVersionModel();
			removeFromInstallSelection(currentSelectionVersion);
			removeFromUninstallSelection(currentSelectionVersion);
			currentSelection.setState(null);
			element.setSelectedVersionTreeElement(null);
		}

		// Reset original state for both root and corresponding child
		PluginState rootState = null;

		Plugin plugin = element.getVersionModel().getParent();

		// Each Root element MUST have an associated child version
		// Determine the child version based on whether the plugin is installed
		// (if so, the associated child should be the installed version) or
		// if the plugin is not installed, the associated child should be the
		// latest
		// released version.

		// If the plugin is installed, get the version that is installed
		PluginVersion childVersion = plugin.getInstalled();

		// Unless it is installed, the child version state is usually null
		PluginState childState = null;

		// Determine the state of the root based on whether the installed
		// version is the latest or if there is an update available.
		// Regardless of the state of the root, the actual child version is
		// always marked as Installed.
		if (childVersion != null) {
			rootState = getOriginalStateOfRootPlugin(childVersion);
			// Corresponding child version is always marked as installed
			// regardless if the plugin has an update available or not
			childState = PluginState.INSTALLED;
		} else {

			// If no version is installed, link the root with the latest
			// released version
			// as this is the version that will be installed if a user performs
			// a "Install"
			// operation on the root.
			childVersion = plugin.getLatestReleasedVersion();
		}

		// Link the child and the root, and set the states for both
		VersionTreeElement associatedElement = getVersionElement(childVersion);
		associatedElement.setState(childState);
		element.setSelectedVersionTreeElement(associatedElement);

		element.setState(rootState);

	}

	/**
	 * Determines if the associated plugin for this version is installed and
	 * what the original state is (either installed to the latest version or has
	 * an update available. The original state is the state prior to any deltas
	 * that were performed on the plugin during the manager session ).
	 * <p>
	 * Not to get confused, this does NOT check whether the specific version is
	 * the one that is installed, but rather if the plugin this version is
	 * associated with is installed, and whether that plugin is installed with
	 * the latest version or it has an update available.
	 * </p>
	 * 
	 * @param version
	 *           whose plugin should be checked if it is installed or not.
	 * @return state of the plugin in the project (installed or update
	 *        available), or null if it is not installed or install state
	 *        cannot be determined.
	 */
	protected PluginState getOriginalStateOfRootPlugin(PluginVersion version) {
		if (version == null) {
			return null;
		}

		if (version.getParent().isInstalled()) {
			return hasUpdate(version) ? PluginState.UPDATE_AVAILABLE
					: PluginState.INSTALLED;
		}
		return null;
	}

	/**
	 * Updates a root element to the latest version. Note that NO validations
	 * are performed on whether an update is possible or not. Validation must be
	 * performed prior to calling this method
	 * 
	 * @param selectedElement
	 */
	protected void updateRootElement(RootTreeElement selectedElement) {
		// Update all only works on Root Elements
		if (selectedElement == null) {
			return;
		}

		VersionTreeElement latestVersionElement = getVersionElement(selectedElement
				.getVersionModel().getParent().getLatestReleasedVersion());
		installVersion(latestVersionElement);
	}

	/**
	 * Iterates through all root elements in the tree and selects any plugin
	 * that is marked as having an update for update.
	 */
	protected void handleUpdateAll() {
		@SuppressWarnings("unchecked")
		List<ITreeElement> rootElements = (List<ITreeElement>) pluginViewer.getTreeViewer()
				.getInput();
		for (ITreeElement rootElement : rootElements) {
			if (rootElement.getPluginState() == PluginState.UPDATE_AVAILABLE
					&& rootElement instanceof RootTreeElement) {
				updateRootElement((RootTreeElement) rootElement);
			}
		}
		validateOperationButtons();
		refreshViewer(true);

	}

	/**
	 * Determines if an update exists for the given plugin version. Note that
	 * certain plugins are NOT marked as having update availables even if newer
	 * versions appear, as their version is directly tied to the Grails version
	 * being used by the project. Such plugins are typically plugins that are
	 * preinstalled when a Grails project is created (e.g., hibernate and
	 * tomcat).
	 * 
	 * @param version
	 *           to check if a newer version exists
	 * @return true if a newer version exists, although in some cases with
	 *        preinstalled plugins false is returned even if newer versions
	 *        exist
	 * 
	 */
	protected boolean hasUpdate(PluginVersion version) {

		boolean isPreinstalledPlugin = isPreinstalled(version);

		if (!isPreinstalledPlugin && version.getParent().hasUpdate()) {
			return true;
		}

		return false;
	}

	protected abstract boolean isPreinstalled(PluginVersion version);

	/**
	 * Checks if the Viewer containing the list of plugins is disposed. It is
	 * disposed (returns true) if the viewer is null, the corresponding tree
	 * widget is null, or the tree widget is disposed. Otherwise it returns
	 * false.
	 * <p/>
	 * In most cases, the state of the viewer does NOT have to be checked as
	 * most viewer access operations occur from the UI thread.
	 * <p/>
	 * However, in some cases where the viewer should react to events from a
	 * non-UI thread, the viewer dispose state should be checked, as in the case
	 * of a viewer refresh from a Grails command executed in another process.
	 */
	protected boolean isViewerDisposed() {
		if (pluginViewer == null) {
			return true;
		}

		Tree tree = pluginViewer.getTreeViewer().getTree();
		if (tree == null) {
			return true;
		}

		return tree.isDisposed();
	}

	/**
	 * Resets the input in the plugins list viewer and clears all selections.
	 * 
	 * @param pluginList
	 *           new input to reset. if viewer is disposed nothing happens. If
	 *           list is null, all entries in the tree are cleared.
	 */
	protected void setViewerInput() {
		clearAllPluginChanges();
		rootElementMap = new HashMap<Plugin, RootTreeElement>();
		childElementMap = new HashMap<PluginVersion, VersionTreeElement>();
		List<RootTreeElement> input = new ArrayList<RootTreeElement>();
		if (pluginList != null && !pluginList.isEmpty()) {
			// refresh list of installed plugins
			for (Plugin plugin : pluginList) {
				RootTreeElement treeElement = getRootElement(plugin);
				if (treeElement != null) {
					restoreOriginalState(treeElement);
					input.add(treeElement);
				}
			}
		}

		if (!isViewerDisposed()) {
			pluginViewer.getTreeViewer().setInput(input);
			refreshViewer(true);
		}
	}

	/**
	 * Refreshes the plugins list viewer in the UI with its current input. Does
	 * not reset the input
	 * 
	 * @param updateLabel
	 *           if all labels and icons should be regenerated. false otherwise
	 */
	protected void refreshViewer(boolean updateLabel) {
		if (!isViewerDisposed()) {
			pluginViewer.getTreeViewer().refresh(updateLabel);
		}
	}

	/**
	 * The tree viewer that displays the list of plugins and allows operations
	 * to be performed on plugin selections. The tree viewer is populated by
	 * tree elements that correspond to plugin models. A root tree element
	 * corresponds to a plugin version that is either available to install or is
	 * currently installed, while the child of the root tree element corresponds
	 * to specific versions of that plugin.
	 * 
	 * @author nisingh
	 * 
	 */
	protected class GrailsBrowserViewer extends TreeViewerComposite {

		public GrailsBrowserViewer(IPluginListColumn[] columns) {
			super(columns, columns[PluginColumnType.PLUGIN_NAME.ordinal()]);
		}

		public void refreshFilter(GrailsFilterType type) {
			PluginStatusFilter filter = getPluginStatusFilter();
			if (filter != null) {
				filter.setFilterType(type);
				refreshViewer(false);
			}

		}

		protected PluginStatusFilter getPluginStatusFilter() {
			ViewerFilter[] filters = GrailsBrowserViewer.this.getTreeViewer()
					.getFilters();
			if (filters != null) {
				for (ViewerFilter filter : filters) {
					if (filter instanceof PluginStatusFilter) {
						return (PluginStatusFilter) filter;
					}
				}

			}
			return null;
		}
	}

	protected class GrailsViewerLabelProvider extends ColumnLabelProvider {

		public void update(ViewerCell cell) {

			Object element = cell.getElement();
			int index = cell.getColumnIndex();

			cell.setText(getColumnText(element, index));
			cell.setImage(getColumnImage(element, index));
			cell.setFont(getFont(element));
		}

		public Image getColumnImage(Object element, int index) {
			if (element instanceof ITreeElement) {
				PluginColumnType[] values = PluginColumnType.values();
				if (index < values.length) {
					PluginColumnType type = values[index];
					if (type == PluginColumnType.PLUGIN_STATE) {
						PluginState state = ((ITreeElement) element)
								.getPluginState();
						if (state != null && iconManager != null) {
							return iconManager.getIcon(state);
						}
					}
				}
			}
			return null;
		}

		public Font getFont(Object element) {
			if (element instanceof ITreeElement) {
				Plugin pluginModel = ((ITreeElement) element)
						.getVersionModel().getParent();
				// Use different font to indicate inplace plugin
				if (pluginModel.isInPlace()) {
					return JFaceResources.getFontRegistry().getBold(
							JFaceResources.DIALOG_FONT);
				}
			}
			return super.getFont(element);
		}

		public String getColumnText(Object element, int index) {

			if (element instanceof ITreeElement) {
				PluginColumnType[] values = PluginColumnType.values();
				if (index < values.length) {
					PluginColumnType type = values[index];
					PluginVersion version = ((ITreeElement) element)
							.getVersionModel();
					String text = null;
					switch (type) {
					case PLUGIN_NAME:
						text = version.getName();
						break;
					case TITLE:
						text = version.getTitle();
						break;
					case VERSION:
						text = version.getVersion();
						break;
					}
					if (text != null) {
						return text;
					}
				}
			}
			return null;
		}

	}

	protected class GrailsViewerColumnSorter extends TreeViewerColumnComparator {

		protected String getCompareString(TreeColumn column, Object rowItem) {
			if (rowItem instanceof ITreeElement) {
				ITreeElement element = (ITreeElement) rowItem;
				PluginColumnType type = getColumnType(column.getText());
				PluginVersion version = element.getVersionModel();
				if (type != null) {
					switch (type) {
					// if sorting by plugin state, it means
					// two equal states are being compared therefore
					// sort plugins with the same state by name
					case PLUGIN_STATE:
					case PLUGIN_NAME:
						return version.getName();
					case TITLE:
						return version.getTitle();
					case VERSION:
						return version.getVersion();
					}
				}
			}
			return null;
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			if (viewer instanceof TreeViewer) {
				Tree tree = ((TreeViewer) viewer).getTree();

				if (e1 instanceof ITreeElement && e2 instanceof ITreeElement) {
					ITreeElement treeElement1 = (ITreeElement) e1;
					ITreeElement treeElement2 = (ITreeElement) e2;
					PluginVersion pluginVersion1 = treeElement1
							.getVersionModel();
					PluginVersion pluginVersion2 = treeElement2
							.getVersionModel();
					PluginState state1 = treeElement1.getPluginState();
					PluginState state2 = treeElement2.getPluginState();

					// These types always are compared with each other, not with
					// any other type
					if (e1 instanceof RootTreeElement
							&& e2 instanceof RootTreeElement) {

						int sortOrder = -1;
						int sortDirection = tree.getSortDirection();

						// Regardless of the sort column, always place in place
						// plugins at top
						boolean isInPlaceElement1 = treeElement1
								.getVersionModel().getParent().isInPlace();
						boolean isInPlaceElement2 = treeElement2
								.getVersionModel().getParent().isInPlace();

						if (isInPlaceElement1) {
							if (isInPlaceElement2) {
								// if both are inplace, sort alphabetically
								return super.compare(viewer, e1, e2);
							} else {
								// the first inplace has higher priority
								sortOrder = -1;
							}
						} else if (isInPlaceElement2) {
							// the second inplace has higher priority
							sortOrder = 1;
						} else {
							// neither are in-place therefore sort based on the
							// type of column that
							// is selected
							TreeColumn sortColumn = tree.getSortColumn();

							// Don't sort if there is no sort column specified
							if (sortColumn == null) {
								return 0;
							}

							PluginColumnType type = getColumnType(sortColumn
									.getText());

							// If the sort column is the state column, sort by
							// state
							if (type == PluginColumnType.PLUGIN_STATE) {

								if (state1 != null) {
									// Compare the two non-null states first
									if (state2 != null) {

										// Use the state definition to determine
										// which comes first
										int stateComparison = state1.ordinal()
												- state2.ordinal();
										if (stateComparison == 0) {
											// if sorting by plugin state, and
											// two equal states are being
											// compared
											// sort plugins with the same state
											// by
											// name
											return super
													.compare(viewer, e1, e2);
										} else {
											sortOrder = stateComparison;
										}

									} else {
										// state 1 has higher order
										sortOrder = -1;
									}
								} else if (state2 != null) {
									// state 2 has higher order
									sortOrder = 1;
								} else {
									// both states are null so use some other
									// criteria to determine sorting order
									return super.compare(viewer, e1, e2);
								}
							} else {
								// if other columns sort alphabetically
								return super.compare(viewer, e1, e2);
							}
						}

						return sortDirection == SWT.UP ? sortOrder : -sortOrder;
					} else if (e1 instanceof VersionTreeElement
							&& e2 instanceof VersionTreeElement) {
						// These types always are compared with each other, not
						// with
						// any other type
						// Versions are sorted by version number regardless of
						// sort column
						String versionID1 = pluginVersion1.getVersion();
						String versionID2 = pluginVersion2.getVersion();
						if (versionID1 != null) {
							if (versionID2 != null) {
								// Highest version has priority
								// thus the reason for the reversed comparison
								return versionID2.compareTo(versionID1);
							} else {
								return -1;
							}
						} else if (versionID2 != null) {
							return 1;
						}
					}
				}
			}
			return super.compare(viewer, e1, e2);
		}

	}

	/**
	 * Represents the top level root tree element in the tree viewer. The root
	 * element represents the particular version of that plugin that has been
	 * marked for selection, or is available for install. A tree element
	 * therefore ALWAYS holds a reference to a particular plugin version model
	 * entity, AND has an associated tree child version element, whose state is
	 * reflected by the root element state (example, if a child version "1.3" of
	 * a particular plugin is marked for uninstall, the root element of for that
	 * plugin will display a "1.3" version and will also be marked with the same
	 * uninstall icon.
	 * <p>
	 * The purpose of the root element is to indicate to the user what current
	 * version of a plugin is selected or installed or available for install ,
	 * and what the state of that version is.
	 * </p>
	 * In all cases, the root element version must match a corresponding version
	 * element. In most cases, the root element state matches the corresponding
	 * version element state, except in the case of a root element marked as
	 * having updates available. In this latter case the corresponding child is
	 * marked with an Installed state while the root is marked with an
	 * "Update available" state
	 * 
	 * @author nisingh
	 * 
	 */
	public class RootTreeElement extends TreeElement {

		private VersionTreeElement associatedVersion;

		/**
		 * A root tree element has two elements associated with it. One is model
		 * version that is either the latest released version for plugins that
		 * are not installed, or the currently installed version for plugins
		 * that are installed. This value is IMMUTABLE for the duration of the
		 * manager session, and it is never null.
		 * <p>
		 * The other is a child version tree element, which reflects the current
		 * tree element selection the user has made. This value is MUTABLE.
		 * </p>
		 * 
		 * @param version
		 *           must not be null.
		 */
		public RootTreeElement(PluginVersion version) {
			super(version);
		}

		/**
		 * Each Root tree element is associated with a selected child version
		 * tree element that reflects the current selection and operation of a
		 * user. This need not be the lastest version of the plugin, but rather
		 * the current selection based on a particular operation. For example,
		 * if a user selected to update to a newer version, or downgrade to an
		 * older version, the root element will reflect this operation, and the
		 * associated version is therefore the newer or older version of the
		 * plugin that the user selected for upgrade or downgrade, respectively.
		 * 
		 * <p>
		 * Note that this is a tree element, NOT a model element. This value is
		 * MUTABLE.
		 * </p>
		 * 
		 * @param associatedVersion
		 */
		public void setSelectedVersionTreeElement(
				VersionTreeElement associatedVersion) {
			this.associatedVersion = associatedVersion;
		}

		/**
		 * Each Root tree element is associated with a selected child version
		 * tree element that reflects the current selection and operation of a
		 * user. This need not be the lastest version of the plugin, but rather
		 * the current selection based on a particular operation. For example,
		 * if a user selected to update to a newer version, or downgrade to an
		 * older version, the root element will reflect this operation, and the
		 * associated version is therefore the newer or older version of the
		 * plugin that the user selected for upgrade or downgrade, respectively.
		 * 
		 * <p>
		 * Note that this is a tree element, NOT a model element. This value is
		 * MUTABLE.
		 * </p>
		 * 
		 * @return associatedVersion
		 */
		public VersionTreeElement getSelectedVersionTreeElement() {
			return associatedVersion;
		}

		/**
		 * 
		 * This is a modified version of getVersionModel. For root elements, the
		 * associated model element is either:
		 * <p>
		 * 1. The original model element that was passed when the root element
		 * was created
		 * </p>
		 * <p>
		 * 2. The model element associated with the current selection, IF a
		 * current selection exists
		 * </p>
		 * 
		 * The current selection has priority over the original model version.
		 * (non-Javadoc)
		 * 
		 * @see org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins.AbstractTreeElement
		 *     #getVersionModel()
		 */
		public PluginVersion getVersionModel() {
			return getSelectedVersionTreeElement() != null ? getSelectedVersionTreeElement()
					.getVersionModel() : super.getVersionModel();
		}

		protected boolean canHandleInstall(PluginState state) {
			// If it is in uninstall state, then install operation can undo the
			// uninstall. If the state is null, it means the plugin can be
			// installed
			if (state == PluginState.SELECT_UNINSTALL || state == null) {
				return true;
			}
			return false;
		}

		protected boolean canHandleUninstall(PluginState state) {
			// Cannot uninstall something that is already marked for uninstall
			// OR something that was never installed in the first place
			if (state == PluginState.SELECT_UNINSTALL || state == null) {
				return false;
			}
			return true;
		}

		protected boolean canHandleUpdate(PluginState currentState) {

			// can't update something that is not currently installed or
			// that is already installed to the latest version
			if (currentState == null || currentState == PluginState.INSTALLED) {
				return false;
			} else if (currentState == PluginState.UPDATE_AVAILABLE) {
				return true;
			} else {
				// Note that the current state of the root may be in a
				// delta-change
				// state, therefore to verify if an update can be performed
				// the ORIGINAL state of the plugin must be checked
				PluginState originalState = getOriginalStateOfRootPlugin(this
						.getVersionModel());
				// Can ONLY enable the update functionality IF the original
				// state was Update available AND the user hasn't yet updated
				// to the latest version. It is possible that a user manually
				// selected an older version to upgrade, which changes the root
				// state to "Select Install", but this should still enable the
				// update button on the root to automatically select the latest
				// version
				if (originalState == PluginState.UPDATE_AVAILABLE) {

					if (currentState == PluginState.SELECT_INSTALL) {
						// if the latest version as stated by the model matches
						// the
						// child tree element version
						// associated with the root, then the root has been
						// updated
						// already
						PluginVersion latestVersion = getVersionModel()
								.getParent().getLatestReleasedVersion();
						PluginVersion associatedModelVersion = getSelectedVersionTreeElement()
								.getVersionModel();
						if (Plugin.isVersionHigher(latestVersion,
								associatedModelVersion)) {
							return true;
						}
					} else if (currentState == PluginState.SELECT_UNINSTALL) {
						// Allow users to jump from SELECT UINSTALL state
						// straight
						// to an updated state
						return true;
					}
				}

				return false;
			}

		}

		protected void handleInstall(PluginState state) {
			// If it is in an select uninstall state, install operation can undo
			// the uninstall
			if (state == PluginState.SELECT_UNINSTALL) {
				restoreOriginalState(this);
			} else if (state == null) {
				VersionTreeElement associatedVersion = getSelectedVersionTreeElement();
				installVersion(associatedVersion);
			}
		}

		protected void handleUninstall(PluginState state) {
			uninstallRoot(this);
		}

		protected void handleUpdate(PluginState state) {
			updateRootElement(this);
		}

	}

	/**
	 * Further specialisation of a tree element that refreshes the viewer when
	 * an operation completes.
	 * 
	 * @author nisingh
	 * 
	 */
	public abstract class TreeElement extends AbstractTreeElement {

		public TreeElement(PluginVersion version) {
			super(version);
		}

		public void performOperation(PluginOperation type) {
			super.performOperation(type);
			refreshViewer(true);
		}
	}

	/**
	 * A tree version element represents a particular version of a plugin.
	 * 
	 * @author nisingh
	 * 
	 */
	public class VersionTreeElement extends TreeElement {

		private RootTreeElement parent;

		public VersionTreeElement(PluginVersion version, RootTreeElement parent) {
			super(version);
			this.parent = parent;
		}

		public RootTreeElement getParent() {
			return this.parent;
		}

		protected boolean canHandleInstall(PluginState state) {
			// If the state is null, install operation can only
			// be performed if the root state is also null,
			// meaning that the plugin is not installed
			if (state == null) {
				PluginState rootState = getParent().getPluginState();
				if (rootState == null) {
					return true;
				} else {
					// it may be in a delta-change state, so determing
					// the ORIGINAL state of the plugin
					PluginState originalState = getOriginalStateOfRootPlugin(this
							.getVersionModel());
					if (originalState == null) {
						return true;
					}
				}
			} else if (state == PluginState.SELECT_UNINSTALL) {
				// Install can be used to Undo uninstall
				return true;
			}

			return false;
		}

		protected boolean canHandleUninstall(PluginState state) {

			if (state == PluginState.SELECT_INSTALL
					|| state == PluginState.INSTALLED) {
				return true;
			}
			return false;
		}

		protected boolean canHandleUpdate(PluginState state) {
			// can only update to something that is not currently marked
			if (state == null) {
				// can only update if the plugin is marked as installed but
				// having updates available
				PluginState currentRootState = getParent().getPluginState();
				if (currentRootState == PluginState.UPDATE_AVAILABLE
						|| currentRootState == PluginState.INSTALLED) {
					return true;
				} else {
					PluginState originalPluginState = getOriginalStateOfRootPlugin(this
							.getVersionModel());
					if (originalPluginState == PluginState.UPDATE_AVAILABLE
							|| originalPluginState == PluginState.INSTALLED) {
						return true;
					}
				}
			}
			return false;
		}

		protected void handleInstall(PluginState state) {

			RootTreeElement rootElement = getParent();
			// Undo uninstall
			if (state == PluginState.SELECT_UNINSTALL) {
				restoreOriginalState(rootElement);
			} else {
				installVersion(this);
			}
		}

		protected void handleUninstall(PluginState state) {
			if (state == PluginState.INSTALLED) {
				uninstallVersion(this);
			} else {
				// Its delta selected, so restore
				RootTreeElement root = getParent();
				restoreOriginalState(root);
			}
		}

		protected void handleUpdate(PluginState state) {
			installVersion(this);
		}
	}

	protected class GrailsViewerContentProvider implements
			ITreePathContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Collection<?>) {
				List<Object> publishedPlugins = new ArrayList<Object>();
				Collection<?> topLevel = (Collection<?>) inputElement;
				for (Object possibleTreeElement : topLevel) {
					if (possibleTreeElement instanceof RootTreeElement) {
						publishedPlugins.add(possibleTreeElement);
					}
				}
				return publishedPlugins.toArray();
			}
			return null;
		}

		public Object[] getChildren(TreePath path) {
			Object lastElement = path.getLastSegment();
			if (lastElement instanceof RootTreeElement) {
				RootTreeElement treeElement = (RootTreeElement) lastElement;
				// One of the children will be the latest
				// version, so no need to add the
				// top level elements.
				PluginVersion topLevelPlugin = treeElement.getVersionModel();
				List<PluginVersion> versions = topLevelPlugin.getParent()
						.getVersions();
				if (versions != null) {
					List<Object> children = new ArrayList<Object>();
					for (PluginVersion version : versions) {
						VersionTreeElement versionTreeElement = getVersionElement(version);
						if (versionTreeElement != null) {
							children.add(versionTreeElement);
						}
					}
					return children.toArray();
				}
			}
			return null;
		}

		public TreePath[] getParents(Object element) {
			if (element instanceof VersionTreeElement) {
				VersionTreeElement pluginElement = (VersionTreeElement) element;
				RootTreeElement parentElementElement = pluginElement
						.getParent();
				if (parentElementElement != null) {
					TreePath path = new TreePath(
							new Object[] { parentElementElement });
					return new TreePath[] { path };
				}
			}
			return new TreePath[] {};
		}

		public boolean hasChildren(TreePath path) {
			return getChildren(path) != null;
		}

		public void dispose() {
			// nothing for now
		}

		public void inputChanged(Viewer viewer, Object e1, Object e2) {
			// nothing for now
		}
	}

	/**
	 * Never null. Returns non-null list of selected elements. May be empty
	 * 
	 * @return non-null list of selected elements. May be empty
	 */
	protected List<ITreeElement> getSelectedTreeElements() {
		IStructuredSelection selection = (IStructuredSelection) pluginViewer
				.getTreeViewer().getSelection();
		List<ITreeElement> selectedElements = new ArrayList<ITreeElement>();

		if (selection != null) {
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object selectedObj = it.next();
				if (selectedObj instanceof ITreeElement) {
					selectedElements.add((ITreeElement) selectedObj);
				}
			}
		}
		return selectedElements;
	}

	protected class PluginSelectionButtonListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent e) {

			Widget widget = e.widget;
			if (widget instanceof Button) {
				Object dataObj = ((Button) widget).getData();
				if (dataObj instanceof PluginOperation) {
					PluginOperation type = (PluginOperation) dataObj;

					// Handle non-selection based operations first
					switch (type) {
					case REFRESH:
						resynchPluginsList(true);
						break;
					case UPDATE_ALL:
						handleUpdateAll();
						break;
					case RESET:
						setViewerInput();
						break;
					case COLLAPSE_ALL:
						collapseAll();
						break;
					default:
						handleSelectionBasedOperations(type);
					}
				}
			}
		}
	}

	/**
	 * Handles button operations that require a selection, like Install,
	 * Uninstall, and Update.
	 * 
	 * @param type
	 */
	protected void handleSelectionBasedOperations(PluginOperation type) {
		if (type == null) {
			return;
		}
		List<ITreeElement> selectedElements = getSelectedTreeElements();

		for (ITreeElement element : selectedElements) {
			if (element.isValidOperation(type)) {
				element.performOperation(type);
			}
		}
		// Once operation performed, refresh the button states
		validateOperationButtons();
	}

	protected void collapseAll() {
		pluginViewer.getTreeViewer().collapseAll();
	}

	protected class ViewerFilterButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {

			Widget widget = e.widget;
			if (widget instanceof Button) {
				Button button = (Button) widget;
				if (button.getSelection()) {
					Object dataObj = button.getData();
					if (dataObj instanceof GrailsFilterType) {
						pluginViewer.refreshFilter((GrailsFilterType) dataObj);
					}
				}
			}
		}
	}

	/**
	 * Filters the tree viewer content based on a filter selection.
	 * 
	 * @author nisingh
	 * 
	 */
	protected class PluginStatusFilter extends ViewerFilter {

		private GrailsFilterType option = GrailsFilterType.SHOW_ALL;

		public void setFilterType(GrailsFilterType option) {
			this.option = option;
		}

		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			// Only filter by RootElement. Always show all child version
			// elements.
			if (element instanceof RootTreeElement) {
				RootTreeElement treeElement = (RootTreeElement) element;
				PluginState state = treeElement.getPluginState();
				// Only filter out top-level items

				if (option != null) {
					switch (option) {
					case SHOW_AVAILABLE_UPDATES_ONLY:
						if (state != PluginState.UPDATE_AVAILABLE) {
							return hasUpdate(treeElement.getVersionModel());
						}
						break;
					case SHOW_CHANGES_ONLY:
						if (state != PluginState.SELECT_INSTALL
								&& state != PluginState.SELECT_UNINSTALL) {
							return false;
						}
						break;
					case SHOW_INSTALLED_ONLY:
						if (state == null) {
							return false;
						} else {
							return treeElement.getVersionModel().getParent()
									.isInstalled();
						}
					}
				}
			}
			return true;
		}
	}

	protected class InternalMessageDialogue extends MessageDialog {

		private boolean closeManager;

		public InternalMessageDialogue(String title, String dialogMessage,
				int dialogueType, boolean closeManager) {
			super(PluginManagerDialog.this.getShell(), title, null,
					dialogMessage, dialogueType, new String[] { "OK" }, 0);
			this.closeManager = closeManager;

		}

		public boolean close() {
			boolean messageClose = super.close();
			if (closeManager) {
				PluginManagerDialog.this.cancelPressed();
			}
			return messageClose;
		}

	}

}
