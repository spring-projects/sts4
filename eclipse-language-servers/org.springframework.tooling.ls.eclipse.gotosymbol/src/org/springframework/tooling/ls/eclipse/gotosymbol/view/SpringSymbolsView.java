package org.springframework.tooling.ls.eclipse.gotosymbol.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.boot.dash.views.sections.ViewPartWithSections;
import org.springframework.tooling.ls.eclipse.gotosymbol.dialogs.GotoSymbolSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.DescriptionSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.InfoFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class SpringSymbolsView extends ViewPartWithSections {

	/**
	 * Adds scroll support to the whole view. You probably want to disable this
	 * if view is broken into pieces that have their own scrollbars
	 */
	private static final boolean ENABLE_SCROLLING = false;

	private final SpringSymbolsViewModel model = new SpringSymbolsViewModel();
	
	private final LiveExpression<String> projectAsString = model.currentProject.apply(p -> {
		System.out.println("project = "+p);
		return p==null ? "null" : p.getName();
	});
	
	private ISelectionListener selectionListener = new ISelectionListener() {
		
		@Override
		public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				Object element = ss.getFirstElement();
				IProject project = getProject(element);
				if (project!=null) {
					model.currentProject.setValue(project);
				}
			} else if (selection instanceof ITextSelection) {
				//Let's assume the selection is in the active editor
				try {
					IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
					if (editor!=null) {
						IEditorInput input = editor.getEditorInput();
						IResource resource = input.getAdapter(IResource.class);
					    if (resource != null) {
					    	IProject project = resource.getProject();
					    	if (project!=null) {
					    		model.currentProject.setValue(project);
					    	}
					    }						
					}
				} catch (Exception e) {
					Log.log(e);
				}
			}
		}

		private IProject getProject(Object element) {
			if (element instanceof IResource) {
				return ((IResource) element).getProject();
			} else if (element instanceof IAdaptable) {
				IResource resource = ((IAdaptable) element).getAdapter(IResource.class);
				if (resource!=null) {
					return resource.getProject();
				}
			}
			return null;
		}
	};
	
	public SpringSymbolsView() {
		super(ENABLE_SCROLLING);
	}

	@Override
	protected List<IPageSection> createSections() throws CoreException {
		List<IPageSection> sections = new ArrayList<>();
		sections.add(new InfoFieldSection(this, "Project", projectAsString));
		sections.add(new GotoSymbolSection(this, model.gotoSymbols));
		ISelectionService selectitonService = getSite().getWorkbenchWindow().getSelectionService();
		selectitonService.addSelectionListener(selectionListener);
		return sections;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		contributeToActionBars();
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fills the pull-down menu for this view (accessible from the toolbar)
	 */
	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void fillLocalToolBar(IToolBarManager manager) {
	}
	
}
