/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml;

import org.dadacoalition.yedit.editor.YEdit;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractYamlEditor extends YEdit {

	private static final ImmutableSet<String> YAML_EXTENSIONS = ImmutableSet.of("yml", "yaml");

	protected ImmutableSet<String> getSupportedFileExtensions() {
		return YAML_EXTENSIONS;
	}

	@Override
	protected boolean canHandleMove(IEditorInput originalElement, IEditorInput movedElement) {
		//See https://issuetracker.springsource.com/browse/STS-4299
		IFile file = (IFile)movedElement.getAdapter(IFile.class);
		if (file!=null) {
			String extension = file.getFileExtension();
			if (extension!=null) {
				extension = extension.toLowerCase();
				return getSupportedFileExtensions().contains(extension);
			}
		}
		return false;
	}
}
