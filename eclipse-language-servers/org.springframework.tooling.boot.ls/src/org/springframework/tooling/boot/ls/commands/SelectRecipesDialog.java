/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import java.util.Arrays;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;
import org.springframework.tooling.boot.ls.commands.RecipeDescriptor.OptionDescriptor;
import org.springframework.tooling.boot.ls.commands.RecipeTreeModel.CheckedState;

@SuppressWarnings("restriction")
public class SelectRecipesDialog extends StatusDialog {
	
	private static final int MARGIN = 5;
	private static final String SELECT_REWRITE_RECIPE_S_FROM_THE_LIST = "Select Rewrite Recipe(s) from the list";
	private static String fgStyleSheet;

	private RecipeTreeModel model;

	public SelectRecipesDialog(Shell parentShell, RecipeTreeModel model) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.model = model;
	}

	@Override
	protected Control createDialogArea(Composite parent) {		
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		form.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
	    form.setLayout(new FillLayout());

	    Composite left = new Composite(form, SWT.NONE);
	    FillLayout layout = new FillLayout();
	    layout.marginHeight = MARGIN;
	    layout.marginWidth = MARGIN;
	    left.setLayout(layout);
	    CheckboxTreeViewer treeViewer = new CheckboxTreeViewer(left);
	    treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof RecipeTreeModel) {
					return ((RecipeTreeModel) inputElement).getRecipeDescriptors();
				}
				return new Object[0];
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof RecipeDescriptor) {
					RecipeDescriptor r = (RecipeDescriptor) parentElement;
					return r.recipeList.toArray(new RecipeDescriptor[r.recipeList.size()]);
				}
				return new RecipeDescriptor[0];
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof RecipeDescriptor) {
					RecipeDescriptor r = (RecipeDescriptor) element;
					return r.recipeList != null && !r.recipeList.isEmpty();
				}
				return false;
			}
		});
	    treeViewer.setLabelProvider(LabelProvider.createTextProvider(input -> {
			if (input instanceof RecipeDescriptor) {
				return ((RecipeDescriptor)input).displayName;
			}
			return "unknown";
	    }));
	    treeViewer.setCheckStateProvider(new ICheckStateProvider() {
			
			@Override
			public boolean isGrayed(Object element) {
				if (element instanceof RecipeDescriptor) {
					RecipeDescriptor r = (RecipeDescriptor) element;
					return model.getCheckedState(r) == CheckedState.GRAYED;
				}
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				if (element instanceof RecipeDescriptor) {
					RecipeDescriptor r = (RecipeDescriptor) element;
					return model.getCheckedState(r) != CheckedState.UNCHECKED;
				}
				return false;
			}
		});
	    treeViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof RecipeDescriptor) {
					RecipeDescriptor d = (RecipeDescriptor) event.getElement();
					if (event.getChecked()) {
						model.check(d);
					} else {
						model.uncheck(d);
					}
					treeViewer.refresh();
					updateStatus();
				}
			}
		});
	    
	    treeViewer.setInput(model);
	    
	    Composite right = new Composite(form, SWT.NONE);
	    layout = new FillLayout();
	    layout.marginHeight = MARGIN;
	    layout.marginWidth = MARGIN;
	    right.setLayout(layout);
	    Browser docViewer = new Browser(right, SWT.NONE);
	    docViewer.setJavascriptEnabled(false);

		Display display= parent.getDisplay();
		docViewer.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		docViewer.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		// Cancel opening of new windows
		docViewer.addOpenWindowListener(event -> event.required= true);

		// Replace browser's built-in context menu with none
		docViewer.setMenu(new Menu(getShell(), SWT.NONE));

	    docViewer.setText(wrapHtml("Select a Recipe on the left to read description"));	    
	    
	    
	    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object o = event.getStructuredSelection().getFirstElement();
				if (o instanceof RecipeDescriptor) {
					docViewer.setText(wrapHtml(buildHtmlDescriptionSnippet((RecipeDescriptor) o)));
				} else {
				    docViewer.setText(wrapHtml("Select a Recipe on the left to read description"));
				}
			}
		});

	    form.setWeights(new int[] { 50, 50 });
	    		
		return form;
	}
	
	
	
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		setTitle("Select Rewrite Recipe");
		parent.getDisplay().asyncExec(() -> updateStatus());
		return c;
	}
	
	private void updateStatus() {
		boolean anythingSelected = Arrays.stream(model.getRecipeDescriptors()).anyMatch(d -> model.getCheckedState(d) != CheckedState.UNCHECKED);
		updateStatus(anythingSelected ? Status.info(SELECT_REWRITE_RECIPE_S_FROM_THE_LIST) : Status.error(SELECT_REWRITE_RECIPE_S_FROM_THE_LIST));
	}

	private String buildHtmlDescriptionSnippet(RecipeDescriptor r) {
		StringBuilder sb = new StringBuilder();
		sb.append("<p>");
		sb.append(r.description);
		sb.append("</p>");
		sb.append("<ul>");
		for (OptionDescriptor option : r.options) {
			if (option.value != null) {
				sb.append("<li>");
				sb.append("<pre>");
				sb.append(option.value);
				sb.append("</pre>");
				sb.append(option.description);
				sb.append("</li>");
			}
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	private static String wrapHtml(String html) {
		/*
		 * No JDT content. Means no JDT CSS part either. Therefore add JDT CSS chunk to it.
		 */
		ColorRegistry registry = JFaceResources.getColorRegistry();
		RGB fgRGB = registry.getRGB("org.eclipse.jdt.ui.Javadoc.foregroundColor"); //$NON-NLS-1$ 
		RGB bgRGB= registry.getRGB("org.eclipse.jdt.ui.Javadoc.backgroundColor"); //$NON-NLS-1$ 

		StringBuilder buffer = new StringBuilder(html);
		HTMLPrinter.insertPageProlog(buffer, 0, fgRGB, bgRGB, getStyleSheet());
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
	
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(getClass())).getDialogSettings();
		IDialogSettings section= settings.getSection(sectionName);
		if (section == null)
			section= settings.addNewSection(sectionName);
		return section;
	}

	/**
	 * Taken from {@link JavadocHover}. It's <code>private</code>. See {@link JavadocHover#getStyleSheet()}.
	 * @return CSS as string
	 */
	private static String getStyleSheet() {
		if (fgStyleSheet == null) {
			fgStyleSheet= JavadocHover.loadStyleSheet("/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
		}
		String css= fgStyleSheet;
		if (css != null) {
			FontData fontData= JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css= HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}

}
