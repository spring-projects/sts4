/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.hover;

import static org.springframework.ide.vscode.commons.util.Renderables.bold;
import static org.springframework.ide.vscode.commons.util.Renderables.concat;
import static org.springframework.ide.vscode.commons.util.Renderables.lineBreak;
import static org.springframework.ide.vscode.commons.util.Renderables.link;
import static org.springframework.ide.vscode.commons.util.Renderables.text;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Nicely formatted hover info for a {@link YTypedProperty}
 *
 * @author Kris De Volder
 */
public class YPropertyHoverInfo {

	public static Renderable create(String contextProperty, YType contextType, YTypedProperty prop) {

		Builder<Renderable> html = ImmutableList.builder();
		if (StringUtil.hasText(contextProperty)) {
			html.add(text(contextProperty));
			html.add(text("."));
		}

		html.add(bold(text(prop.getName())));

		html.add(lineBreak());

		YType type = prop.getType();
		if (type != null) {
			html.add(link(type.toString(), /* no URL */ null));
		}

		Renderable description = prop.getDescription();
		if (description != null) {
			html.add(lineBreak());
			html.add(description);
		}

		return concat(html.build());
	}

}
