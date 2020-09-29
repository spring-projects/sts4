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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.CompositeCommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.IFrameworkCommand;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.JavaParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ParameterKind;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist.JavaContentAssistUIAdapter;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.BooleanParameterEditor;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.IParameterEditor;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.IProjectSelectionChangeListener;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.IUIChangeListener;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.ParameterEditorFactory;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors.UIEvent;


/**
 * Displays editors for each parameter in a command.
 * <p>
 * Editors are aligned and spaced using equal units. The amount of space between
 * the edge of the main composite and all the editors can be controlled by this
 * wizard page.
 * </p>
 * <p>
 * Usually most editors each are displayed in 2 column composites, where the
 * first column contains a label with the parameter name, and is left-aligned,
 * and the second column contains all the remaining controls for the editor. The
 * wizard defines the composite that is passed to each editor, that way it can
 * control the spacing and alignment of the editor layout, and maintain
 * exactness for all of them.
 * </p>
 * @author Nieraj Singh
 */
public class GenericWizardCommandParametersPage extends
		AbstractGenericWizardPage implements IUIChangeListener {

	private IFrameworkCommand command;

	private Point longestLabelWidth;
	private List<IProjectSelectionChangeListener> projectSelectionListeners;

	/**
	 * Keeps track of which characters have mneumonics for Alt+? keyboard
	 * shortcut navigation NOTE that there are RESERVED letters that are already
	 * used by the wizard for the Next, Back and Finish buttons
	 */
	public static final String RESERVED_KEYBOARD_SHORTCUTS = "BbNnFf";
	private StringBuffer mneumonicsKeyBoardShortcuts = new StringBuffer()
			.append(RESERVED_KEYBOARD_SHORTCUTS);

	// This can't be a mneumonic, therefore use it to determine that no valid
	// letters were encountered in a label when deciding to find a suitable
	// shortcut
	private static final char NON_MNEUMONIC = '\0';

	public GenericWizardCommandParametersPage(IFrameworkCommand command) {
		super("Configure command parameters");
		setDescription("Configure the command parameters. Finishing the wizard will execute the command. Required fields must have values before the wizard can complete.");
		this.command = command;
		if (command != null) {
			setTitle(command.getCommandDescriptor().getName());
		}
		projectSelectionListeners = new ArrayList<IProjectSelectionChangeListener>();
	}

	/**
	 * Returns the command instance that was edited by this page. Cannot be
	 * null.
	 * 
	 * @return non-null command instance that was edited by this page.
	 */
	public IFrameworkCommand getCommand() {
		return command;
	}

	protected Composite createPageArea(Composite parent) {
		if (command == null) {
			setErrorMessage("No command specified. Please select a command first");
			return parent;
		}

		List<ICommandParameter> parameters = command.getParameters();

		if (parameters == null) {
			return parent;
		}

		Composite parameterArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parameterArea);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parameterArea);

		if (groupRequiredParameters()) {
			// Group required parameters first
			List<ICommandParameter> requiredParameters = new ArrayList<ICommandParameter>();
			List<ICommandParameter> optionalParameters = new ArrayList<ICommandParameter>();

			for (ICommandParameter parameter : parameters) {
				boolean isMandatory = parameter.getParameterDescriptor()
						.isMandatory();
				if (isMandatory) {
					requiredParameters.add(parameter);
				} else {
					optionalParameters.add(parameter);
				}
			}

			if (!requiredParameters.isEmpty()) {
				createParameterEditors(parameterArea, "Required Parameters",
						requiredParameters);
			}

			if (!optionalParameters.isEmpty()) {
				createParameterEditors(parameterArea, "Optional Parameters",
						optionalParameters);
			}

		} else {
			createParameterEditors(parameterArea, "Parameters", parameters);
		}

		checkPageComplete(false);

		return parameterArea;
	}

	protected Composite createParameterEditors(Composite parent,
			String groupLabel, List<ICommandParameter> parameters) {
		Group parameterGroup = new Group(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parameterGroup);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parameterGroup);
		parameterGroup.setText(groupLabel);

		ScrolledComposite scrolledParameterArea = new ScrolledComposite(
				parameterGroup, SWT.V_SCROLL);

		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(scrolledParameterArea);

		scrolledParameterArea.setFont(parent.getFont());
		scrolledParameterArea.setExpandHorizontal(true);
		scrolledParameterArea.setExpandVertical(true);

		// Contains the actual editors that need to be placed under the
		// scrollable composite
		Composite editorContent = new Composite(scrolledParameterArea, SWT.NONE);

		GridLayoutFactory
				.fillDefaults()
				.numColumns(1)
				.margins(getEditorAreaWidthMargin(),
						getEditorAreaHeightMargin())
				.spacing(SWT.DEFAULT, getVerticalEditorControlSpacing())
				.applyTo(editorContent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorContent);

		createEditors(editorContent, parameters);

		// Content has to be set AFTER the editors are created
		scrolledParameterArea.setContent(editorContent);
		scrolledParameterArea.setMinSize(editorContent.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		return scrolledParameterArea;
	}

	/**
	 * If true group mandatory/required parameters first. If false create the
	 * parameter controls in the order that they appear in the command
	 * descriptor. This does NOT rearrange the order of the parameters in the
	 * command. It only affects the order of the controls in the UI, but the
	 * order of the parameters in the command itself is unchanged.
	 * 
	 * @return true if group mandatory parameters. False if parameter order is
	 *        to be preserved.
	 */
	protected boolean groupRequiredParameters() {
		return getGenericCommandWizard().groupRequiredParameters();
	}

	/**
	 * Converts a label into a shortcut-enabled label, IF one of the characters
	 * in the label hasn't already been used by another label. If all the
	 * characters in the label have already been used, the same label is
	 * returned and no shortcuts are enabled for that control
	 * 
	 * @param label
	 *           to convert into shortcut-enabled version
	 * @return shortcut-enabled version if a letter is found that can be used as
	 *        a shortcut, or the same label if no shortcut letters are found
	 */
	protected String getControlLabelWithKeyboardShortcut(String label) {
		if (label == null || label.length() == 0) {
			return label;
		}

		// This can't be a mneumonic so use it to decide if a mneumonic was
		// found or not
		char possibleMneumonic = NON_MNEUMONIC;

		int mneumonicLocation = 0;
		for (; mneumonicLocation < label.length(); mneumonicLocation++) {
			if (!isMneumonicShortcutAssigned(label.charAt(mneumonicLocation))) {
				possibleMneumonic = label.charAt(mneumonicLocation);
				mneumonicsKeyBoardShortcuts.append(possibleMneumonic);
				break;
			}
		}

		if (possibleMneumonic != NON_MNEUMONIC) {
			// Add support for the Alt+? navigation which means adding an '&'
			// before the
			// shortcut letter
			String keyBoardShortCutLabel = new StringBuffer(label).insert(
					mneumonicLocation, '&').toString();
			return keyBoardShortCutLabel;
		}

		return label;
	}

	/**
	 * Determine if a character has already been used as a shortcut for another
	 * label control. If so return true. Else return false.
	 * 
	 * @param ch
	 * @return true if character is already used as a shortcut. False otherwise.
	 */
	protected boolean isMneumonicShortcutAssigned(char ch) {
		for (int i = 0; i < mneumonicsKeyBoardShortcuts.length(); i++) {
			if (mneumonicsKeyBoardShortcuts.charAt(i) == ch) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create editors for each parameter in the parameter list. All editors are
	 * added to the specified Composite parent.
	 * 
	 * @param parent
	 *           parent composite containing the editor controls
	 * @param parameters
	 *           list of parameters whose editors should be added to the parent
	 *           composite
	 */
	protected void createEditors(Composite parent,
			List<ICommandParameter> parameters) {
		if (parameters == null) {
			return;
		}
		// Use index to iterate to keep track of consecutive boolean editors
		for (int createdElementIndex = 0; createdElementIndex < parameters
				.size();) {
			ICommandParameter parameter = parameters.get(createdElementIndex);
			ParameterKind kind = parameter.getParameterDescriptor()
					.getParameterKind();

			if (kind == null) {
				continue;
			}

			switch (kind) {
			case COMPOSITE:
				createCompositeEditor(parent,
						(CompositeCommandParameter) parameter);
				break;
			default:
				createdElementIndex = createEditor(parent, parameters,
						createdElementIndex, null, true);
			}

			createdElementIndex++;
		}
	}

	/**
	 * Creates a single Editor for the parameter instance referenced by the
	 * index argument, EXCEPT in the case of consecutive Boolean parameters.
	 * This method will create n number of Boolean editors that appear in
	 * consecutive order, and will return the index of the last created Boolean
	 * editor.
	 * <p>
	 * Example: if a list of parameters contains the following: 1 Java
	 * parameter, 3 Boolean Parameters, and 1 Combo parameter, in the stated
	 * order, this method is called 3 times, once for the java parameter, once
	 * for the 3 Boolean parameters, and once for the Combo parameter. The 3
	 * consecutive Boolean parameters are all handled in one call.
	 * </p>
	 * 
	 * @param kind
	 *           type of parameter editor to create
	 * @param parent
	 *           Composite parent for the editor
	 * @param parameters
	 *           list of parameter instances
	 * @param currentIndex
	 *           index of the parameter instance that needs to be passed into
	 *           the editor
	 * @param showLabel
	 *           whether label controls should be shown for the editor.
	 * @return index of the last editor that was created
	 */
	protected int createEditor(Composite parent,
			List<ICommandParameter> parameters, int currentIndex,
			List<IParameterEditor> editorList, boolean showLabel) {
		if (parameters != null && currentIndex >= 0
				&& currentIndex < parameters.size()) {
			ICommandParameter parameter = parameters.get(currentIndex);
			IParameterEditor editor = null;

			ParameterKind kind = parameter.getParameterDescriptor()
					.getParameterKind();
			switch (kind) {
			case BOOLEAN:
				// Add the boolean editors separately, as they are arranged
				// differently
				currentIndex = addBooleanEditors(parent, parameters,
						currentIndex, editorList);
				break;
			case BASE:
				editor = getTextEditor(parameter, showLabel);
				break;
			case JAVA_TYPE:
				editor = getJavaEditor(parameter, showLabel);
				break;
			case COMBO:
				editor = getComboEditor(parameter, showLabel);
				break;
			}

			if (editor != null) {
				addAndConfigureEditor(parent, editor, editorList);
			}
		}
		return currentIndex;
	}

	/**
	 * Certain types of editors like base, Java and combo editors have labels
	 * that contain the parameter name. This method creates a label for each of
	 * these editors and sets the size of all the labels to a fixed width such
	 * that all labeled controls are properly aligned.
	 * 
	 * @param editor
	 * @param parent
	 */
	protected void addLabel(IParameterEditor editor, Composite parent) {
		if (editor == null || parent == null) {
			return;
		}

		ICommandParameterDescriptor parameter = editor.getParameterDescriptor();
		String parameterName = getControlLabelWithKeyboardShortcut(parameter
				.getName());

		Label parameterNameLabel = new Label(parent, SWT.READ_ONLY);

		parameterNameLabel.setText(parameterName + ": ");
		parameterNameLabel.setToolTipText(parameter.getDescription());

		GridDataFactory.fillDefaults().grab(false, false)
				.align(SWT.FILL, SWT.CENTER).applyTo(parameterNameLabel);

		// Set the label dimensions based on the longest label string
		Point labelSize = getLongestLabelSize();

		if (labelSize != null) {
			GridData data = (GridData) parameterNameLabel.getLayoutData();
			int heightHint = labelSize.y;
			if (heightHint > 0) {
				data.heightHint = heightHint;
			}
			int widthHint = labelSize.x;
			if (widthHint > 0) {
				data.widthHint = widthHint;
			}
		}
	}

	/**
	 * Usually editors notify the page when values have been entered to allow
	 * the page to determine if the wizard can complete or not
	 */
	public void handleUIEvent(UIEvent event) {
		if ((event.getType() & UIEvent.VALUE_SET) == UIEvent.VALUE_SET) {
			checkPageComplete(true);
		}
	}

	/**
	 * 
	 * @param parent
	 * @param parameters
	 * @param currentIndex
	 * @param showLabel
	 * @return
	 */
	protected void createCompositeEditor(Composite parent,
			CompositeCommandParameter compositeParameter) {
		List<ICommandParameter> childParameters = compositeParameter
				.getParameters();
		if (!childParameters.isEmpty()) {
			List<IParameterEditor> childEditors = new ArrayList<IParameterEditor>();
			for (int lastCreatedIndex = 0; lastCreatedIndex < childParameters
					.size();) {
				boolean showLabel = lastCreatedIndex == 0 ? true : false;
				lastCreatedIndex = createEditor(parent, childParameters,
						lastCreatedIndex, childEditors, showLabel);
				lastCreatedIndex++;
			}
			new CompositeParameterController(childEditors).start();
		}
	}

	/**
	 * Inspects all editors to determine if any required parameters have their
	 * values filled. The wizard's finish button is disabled if any required
	 * parameters need to be filled in.
	 * <p>
	 * In addition, if the set error message flag is set to true, an error
	 * message will be printed to the wizard dialogue indicating which
	 * parameters still need to be completed.
	 * </p>
	 * 
	 * @param setErrorMessage
	 */
	protected void checkPageComplete(boolean setErrorMessage) {
		boolean isPageComplete = true;
		setErrorMessage(null);
		List<ICommandParameter> parameters = getCommand().getParameters();
		if (parameters != null) {
			StringBuffer bufferMessage = new StringBuffer();

			bufferMessage.append("Missing required value for: ");
			boolean first = true;
			for (ICommandParameter parameter : parameters) {
				if (parameter.getParameterDescriptor().isMandatory()
						&& !parameter.hasValue()) {
					if (first) {
						first = false;
					} else {
						bufferMessage.append(", ");
					}
					bufferMessage.append(parameter.getParameterDescriptor()
							.getName());

					isPageComplete &= false;
				}
			}

			if (!isPageComplete && setErrorMessage) {
				setErrorMessage(bufferMessage.toString());
			}
		}
		setPageComplete(isPageComplete);
	}

	/**
	 * Creates controls for boolean editors that appear in consecutive order
	 * starting from the given index.
	 * <p/>
	 * If there are n consecutive boolean editors, this will create n editors
	 * arranged in a composite with m columns.
	 * <p/>
	 * Returns the index of the last boolean editor created in the series of
	 * consecutive boolean editors, or -1 if no boolean editors are found
	 * 
	 * @param parent
	 * @param parameters
	 * @param booleanEditorIndex
	 * @return
	 */
	protected int addBooleanEditors(Composite parent,
			List<ICommandParameter> parameters, int booleanEditorIndex,
			List<IParameterEditor> editors) {
		if (parameters == null || booleanEditorIndex < 0
				|| booleanEditorIndex >= parameters.size()) {
			return -1;
		}

		if (parameters.get(booleanEditorIndex).getParameterDescriptor()
				.getParameterKind() != ParameterKind.BOOLEAN) {
			return -1;
		}

		Composite booleanArea = new Composite(parent, SWT.NONE);

		// Indent the composite containing the boolean controls such that they
		// are aligned with the text controls for other editors.
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false)
				.applyTo(booleanArea);
		GridData booleanAreaData = new GridData(SWT.FILL, SWT.FILL, false,
				false);

		booleanAreaData.horizontalIndent = getNonLabelControlIndentation();

		booleanArea.setLayoutData(booleanAreaData);

		int consecutiveBooleanIndex = booleanEditorIndex;

		for (; consecutiveBooleanIndex < parameters.size();) {
			ICommandParameter parameter = parameters
					.get(consecutiveBooleanIndex);

			if (parameter.getParameterDescriptor().getParameterKind() == ParameterKind.BOOLEAN) {
				// Indent the boolean editor by the longest label length
				IParameterEditor editor = ParameterEditorFactory
						.getBooleanParameterEditor(parameter);
				if (editor == null) {
					continue;
				}
				addAndConfigureEditor(booleanArea, editor, editors);

				// assign current index before incrementing
				booleanEditorIndex = consecutiveBooleanIndex++;

			} else {
				break;
			}
		}

		// return index of last boolean editor created
		return booleanEditorIndex;
	}

	/**
	 * Adds an editor to the given editor list.
	 * 
	 * @param parent
	 * @param editor
	 * @param editorList
	 */
	protected void addAndConfigureEditor(Composite parent,
			IParameterEditor editor, List<IParameterEditor> editorList) {
		editor.addUIChangeListener(this);

		createEditorControl(parent, editor);

		// As Boolean editors do not have labels, but still should have
		// keyboard shortcuts, configure the Boolean editors to have keyboard
		// shortcuts
		if (editor.getParameterDescriptor().getParameterKind() == ParameterKind.BOOLEAN) {
			Button button = ((BooleanParameterEditor) editor)
					.getButtonControl();
			String buttonText = button.getText();
			button.setText(getControlLabelWithKeyboardShortcut(buttonText));
		}

		if (editorList != null) {
			editorList.add(editor);
		}
	}

	/**
	 * Creates an editor with label and Text controls. It sets the size of the
	 * label control based on the longest label name it can find.
	 * 
	 * @param parent
	 * @param parameter
	 * @return created editor, or null if not created.
	 */
	protected IParameterEditor getTextEditor(ICommandParameter parameter,
			boolean showLabel) {
		if (parameter == null) {
			return null;
		}

		IParameterEditor editor = ParameterEditorFactory.getParameterEditor(
				parameter, showLabel);
		return editor;
	}

	protected IParameterEditor getJavaEditor(ICommandParameter parameter,
			boolean showLabel) {
		if (parameter == null) {
			return null;
		}

		if (parameter.getParameterDescriptor().getParameterKind() != ParameterKind.JAVA_TYPE) {
			return null;
		}

		GenericCommandWizard wizard = getGenericCommandWizard();

		ICommandParameterDescriptor descriptor = parameter
				.getParameterDescriptor();

		JavaContentAssistUIAdapter adapter = wizard != null
				&& (descriptor instanceof JavaParameterDescriptor) ? wizard
				.getJavaContentAssistUIAdapter((JavaParameterDescriptor) descriptor)
				: null;
		IParameterEditor editor = ParameterEditorFactory
				.getJavaParameterEditor(parameter, adapter, showLabel);
		if (adapter instanceof IProjectSelectionChangeListener) {
			IProjectSelectionChangeListener listener = (IProjectSelectionChangeListener) adapter;
			projectSelectionListeners.add(listener);
			// Set the current selection.
			listener.projectSelectionChanged(wizard.getSelectedProject());
		}

		return editor;
	}

	protected void setProjectInWizard(IProject selectedProject) {
		super.setProjectInWizard(selectedProject);
		// notify listeners that a project selection has changed
		for (IProjectSelectionChangeListener listener : projectSelectionListeners) {
			listener.projectSelectionChanged(selectedProject);
		}
	}

	/**
	 * Creates the editor controls. Each editor is created with a 1 column
	 * composite where all editor controls are displayed. In addition, a label
	 * control with the parameter name is added if the editor requests a name
	 * label to be created.
	 * 
	 * @param parent
	 * @param editor
	 */
	protected void createEditorControl(Composite parent, IParameterEditor editor) {
		if (editor == null) {
			return;
		}

		Composite editorRow = new Composite(parent, SWT.NONE);

		boolean requiresLabel = editor.requiresParameterNameLabel();

		// One column should be reserved for the label
		// the other column should contain the composite that
		// in turn contains all of the editor controls
		int columns = requiresLabel ? 2 : 1;

		GridLayoutFactory.fillDefaults().numColumns(columns).margins(0, 0)
				.spacing(getHorizontalEditorControlSpacing(), SWT.DEFAULT)
				.applyTo(editorRow);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(editorRow);

		GridData editorControlsGridData = new GridData(SWT.FILL, SWT.CENTER,
				true, false);

		if (editor.requiresParameterNameLabel()) {
			addLabel(editor, editorRow);
		} else if (editor.getParameterDescriptor().getParameterKind() != ParameterKind.BOOLEAN) {
			// This handles the case where editors form part of a composite
			// editor, and therefore
			// have no labels of there own but still need to be indented the
			// same distance as the label as to keep all controls, whether they
			// have labels or not, propertly aligned.
			// Boolean parameters don't need to be indented, as the
			// composite containing the boolean parameters is already indented

			editorControlsGridData.horizontalIndent = getNonLabelControlIndentation();
		}

		// Composite that contains all the editor controls.
		Composite editorControls = new Composite(editorRow, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.applyTo(editorControls);
		editorControls.setLayoutData(editorControlsGridData);

		editor.createControls(editorControls);
	}

	/**
	 * Return the indentation of all parameter controls that have no label, like
	 * boolean parameters, or parameters that are part of a composite parameter
	 * 
	 * @return
	 */
	protected int getNonLabelControlIndentation() {
		return getLongestLabelSize().x + getHorizontalEditorControlSpacing();
	}

	/**
	 * Creates and formats a combo editor.
	 * 
	 * @param parent
	 * @param parameter
	 * @return
	 */
	protected IParameterEditor getComboEditor(ICommandParameter parameter,
			boolean showLabel) {
		IParameterEditor editor = ParameterEditorFactory.getParameterEditor(
				parameter, showLabel);
		return editor;
	}

	/**
	 * Given a list of parameters, find the parameter name that is the longest,
	 * as this will be used to compute the pixel equivalent width for the label
	 * column.
	 * 
	 * @param parent
	 * @param parameters
	 * @return longest parameter name that is found in pixels.
	 */
	protected Point getLongestLabelSize() {

		if (longestLabelWidth != null) {
			return longestLabelWidth;
		}
		int length = SWT.DEFAULT;
		int charLength = 0;
		List<ICommandParameter> parameters = getCommand().getParameters();
		if (parameters != null) {
			for (ICommandParameter parameter : parameters) {
				// only compute label length of Base and Java editors
				ParameterKind kind = parameter.getParameterDescriptor()
						.getParameterKind();
				if (kind != ParameterKind.BOOLEAN) {
					String name = parameter.getParameterDescriptor().getName();
					if (name != null) {
						int nameLength = name.length();
						if (nameLength > charLength) {
							charLength = nameLength;
						}
					}
				}
			}
		}
		if (charLength > 0) {
			Control control = getShell();
			GC gc = new GC(control);
			Font requiredLabelFont = getRequiredParameterFont();
			gc.setFont(requiredLabelFont != null ? requiredLabelFont : control
					.getFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			// Increment the length by a few pixels to cover colon that may be
			// appended
			length = Dialog
					.convertWidthInCharsToPixels(fontMetrics, charLength);
			gc.dispose();
		}
		longestLabelWidth = new Point(length, -1);
		longestLabelWidth.x += getLabelNameSeparatorOffset();
		return longestLabelWidth;
	}

	/**
	 * 
	 * @return optional font type to use (bold, italic, etc..) used for to
	 *        highlight required parameters. Return null to use the default
	 *        dialog font.
	 */
	protected Font getRequiredParameterFont() {
		return JFaceResources.getFontRegistry().getBold(
				JFaceResources.DIALOG_FONT);
	}

	/**
	 * Increase the label width enough to include separator characters like
	 * colon, etc..
	 * 
	 * @return
	 */
	protected int getLabelNameSeparatorOffset() {
		return 5;
	}

	/**
	 * 
	 * @return width margin between the editor controls and the composite edge
	 */
	protected int getEditorAreaWidthMargin() {
		return 5;
	}

	/**
	 * 
	 * @return height margin between the editor controls and the parent
	 *        composite edge
	 */
	protected int getEditorAreaHeightMargin() {
		return 10;
	}

	public static class CompositeParameterController implements
			IUIChangeListener {

		private List<IParameterEditor> editors;

		public CompositeParameterController(List<IParameterEditor> editors) {
			this.editors = editors;
		}

		public void start() {
			if (editors != null) {
				for (IParameterEditor editor : editors) {
					editor.addUIChangeListener(CompositeParameterController.this);
				}
			}
		}

		public void handleUIEvent(UIEvent event) {
			if ((event.getType() & UIEvent.CLEAR_VALUE_EVENT) == UIEvent.CLEAR_VALUE_EVENT) {

				IUIChangeListener notifier = event.getNotifier();
				if (editors != null) {
					UIEvent clearControlEvent = new UIEvent(null,
							CompositeParameterController.this,
							UIEvent.CLEAR_VALUE_EVENT);
					for (IParameterEditor editor : editors) {
						if (notifier != editor
								&& editor instanceof IUIChangeListener) {
							((IUIChangeListener) editor)
									.handleUIEvent(clearControlEvent);
						}
					}
				}
			}
		}
	}

	/**
	 * Horizontal spacing between the parameter label and the rest of the
	 * editor's controls
	 * 
	 * @return spacing value. Default SWT value used if less than zero
	 */
	protected int getHorizontalEditorControlSpacing() {
		return 4;
	}

	/**
	 * Vertical spacing between the editor rows.
	 * 
	 * @return spacing value. Default SWT value used if less than zero
	 */
	protected int getVerticalEditorControlSpacing() {
		return 5;
	}

}
