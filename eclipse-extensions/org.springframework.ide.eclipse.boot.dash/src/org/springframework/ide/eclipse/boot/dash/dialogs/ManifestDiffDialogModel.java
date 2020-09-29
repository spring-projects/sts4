/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import org.eclipse.compare.CompareEditorInput;

/**
 * @author Kris De Volder
 * @author Alex Boyko
 */
public class ManifestDiffDialogModel {

	private CompareEditorInput input;

	public ManifestDiffDialogModel(CompareEditorInput input) {
		this.input = input;
	}

	public enum Result {
		CANCELED,
		USE_MANIFEST,
		FORGET_MANIFEST
	}

	public CompareEditorInput getInput() {
		return input;
	}

}
