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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.legacyconversion;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.legacyconversion.IConversionConstants;

/**
 * 
 * @author Andrew Eisenberg
 * @since 3.0.0
 */
public class ListMessageDialog extends MessageDialogWithToggle implements IConversionConstants {

    class TableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProject[]) {
                return (IProject[]) inputElement;
            }
            return null;
        }
        
        public void dispose() { }
        public void inputChanged(Viewer viewer2, Object oldInput, Object newInput) { }
    }
    private static final IPreferenceStore PREFERENCE_STORE = FrameworkCoreActivator.getDefault().getPreferenceStore();
    private static final String PREFERENCE_QUESTION = "Don't show this dialog again."; //$NON-NLS-1$
    private static final String TITLE = "Should convert legacy STS projects?"; //$NON-NLS-1$

    private final IProject[] legacyProjects;
    private IProject[] checkedLegacyProjects;
    
    private CheckboxTableViewer viewer;

    /**
     * Opens the legacy maven project conversion dialog focusing on the selected projects
     * @param legacyProjects
     * @return
     */
    public static IProject[] openViewer(Shell shell, IProject[] legacyProjects) {
        ListMessageDialog dialog = new ListMessageDialog(shell, legacyProjects);
        int res = dialog.open();
        PREFERENCE_STORE.setValue(AUTO_CHECK_FOR_LEGACY_STS_PROJECTS, ! dialog.getToggleState());
        if (res == IDialogConstants.YES_ID) {
            return dialog.getAllChecked();
        } else {
            return null;
        }
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.YES_ID) {
            Object[] checkedElements = viewer.getCheckedElements();
            checkedLegacyProjects = new IProject[checkedElements.length];
            System.arraycopy(checkedElements, 0, checkedLegacyProjects, 0, checkedElements.length);
            
            // don't want to do workspace preferences again
            PREFERENCE_STORE.setValue(LEGACY_MIGRATION_ALREADY_DONE, true);
        }
        super.buttonPressed(buttonId);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }

    public ListMessageDialog(Shell shell, IProject[] legacyProjects) {
        super(shell, TITLE, null, createMessage(legacyProjects), QUESTION, new String[] { IDialogConstants.YES_LABEL,
                IDialogConstants.NO_LABEL }, 0, PREFERENCE_QUESTION, 
                PREFERENCE_STORE.getBoolean(AUTO_CHECK_FOR_LEGACY_STS_PROJECTS));
        this.legacyProjects = legacyProjects;
    }

    protected Control createCustomArea(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns = 2;
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        
        viewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        gd.verticalSpan = 2;
        viewer.getTable().setLayoutData(gd);
        viewer.setContentProvider(new TableContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(legacyProjects);
        viewer.setAllChecked(true);
        applyDialogFont(viewer.getControl());
        createButton(parent, "Select all", new SelectionAdapter() { //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });
        createButton(parent, "Select none", new SelectionAdapter() { //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });
        
        return viewer.getControl();
    }
    
    protected Button createButton(Composite parent, String label, SelectionListener listener) {
        return createButton(parent, label, SWT.PUSH, listener);
    }
    
    protected Button createButton(Composite parent, String label, int style, SelectionListener listener) {
        Button button= new Button(parent, SWT.PUSH);
        button.setFont(parent.getFont());
        button.setText(label);
        button.addSelectionListener(listener);
        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.FILL;
        gd.grabExcessHorizontalSpace= false;
        gd.verticalAlignment= GridData.BEGINNING;
        gd.widthHint = 100;

        button.setLayoutData(gd);

        return button;
    }
    
    IProject[] getAllChecked() {
        return checkedLegacyProjects;
    }
    
    private static String createMessage(IProject[] allLegacyProjects) {
        StringBuilder sb = new StringBuilder();
        if (allLegacyProjects.length > 1) {
            sb.append("The following legacy STS projects have been found:\n"); //$NON-NLS-1$
        } else {
            sb.append("The following legacy STS project has been found:\n"); //$NON-NLS-1$
        }
        if (allLegacyProjects.length > 1) {
            sb.append("\n** These projects may not compile until they are upgraded to STS 3.0. **\n\n"); //$NON-NLS-1$
        } else {
            sb.append("\n** This project may not compile until it is upgraded to STS 3.0. **\n\n"); //$NON-NLS-1$
        }
        sb.append("Do you want to upgrade now?\n" + //$NON-NLS-1$
                "You can choose to upgrade later by going to:\n" + //$NON-NLS-1$
                "Project -> Configure -> Convert legacy STS projects..."); //$NON-NLS-1$
        return sb.toString();
    }
}
