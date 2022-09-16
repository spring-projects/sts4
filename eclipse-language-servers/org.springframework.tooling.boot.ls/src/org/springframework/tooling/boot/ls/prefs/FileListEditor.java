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
package org.springframework.tooling.boot.ls.prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.PathEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

public class FileListEditor extends PathEditor {
	
	private String lastPath;
	
	private String dirChooserLabelText;
	
	private List<String> fileFilters;
	
	public FileListEditor(String name, String labelText,
			String dirChooserLabelText, Composite parent, List<String> fileFilters) {
		super(name, labelText, dirChooserLabelText, parent);
		this.dirChooserLabelText = dirChooserLabelText;
		this.fileFilters = fileFilters;
	}

	@Override
	protected String getNewInputObject() {

		FileDialog dialog = new FileDialog(getShell(), SWT.SHEET);
		dialog.setFilterExtensions(fileFilters.toArray(String[]::new));
		if (dirChooserLabelText != null) {
			dialog.setText(dirChooserLabelText);
		}
		if (lastPath != null) {
			if (new File(lastPath).exists()) {
				dialog.setFilterPath(lastPath);
			}
		}
		String file = dialog.open();
		if (file != null) {
			String parentFolder = new File(file).getParent();
			if (parentFolder == null) {
				return null;
			}
			lastPath = parentFolder;
		}
		return file;
	}
	
	public static List<String> getValuesFromPreference(String rawValue) {
		StringTokenizer st = new StringTokenizer(rawValue, File.pathSeparator
				+ "\n\r");//$NON-NLS-1$
		ArrayList<String> l = new ArrayList<>();
		while (st.hasMoreElements()) {
			l.add((String)st.nextElement());
		}
		return l;
	}

}
