/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.metadata.hints;

import static org.springframework.ide.vscode.commons.util.Renderables.bold;
import static org.springframework.ide.vscode.commons.util.Renderables.concat;
import static org.springframework.ide.vscode.commons.util.Renderables.paragraph;

import org.springframework.ide.vscode.commons.util.Renderable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ValueHintHoverInfo {

	public static Renderable create(StsValueHint hint) {
		Builder<Renderable> builder = ImmutableList.builder();
		builder.add(bold(""+hint.getValue()));
		builder.add(paragraph(hint.getDescription()));
		return concat(builder.build());
	}

}
