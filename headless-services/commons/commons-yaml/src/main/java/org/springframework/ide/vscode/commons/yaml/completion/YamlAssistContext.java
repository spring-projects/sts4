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
package org.springframework.ide.vscode.commons.yaml.completion;

import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.yaml.path.YamlNavigable;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public interface YamlAssistContext extends YamlNavigable<YamlAssistContext> {
	Collection<ICompletionProposal> getCompletions(YamlDocument doc, SNode current, int offset) throws Exception;

	//TODO: conceptually... the right thing would be to only implement the second of these
	// two methods and get rid of the first one.
	Renderable getHoverInfo();
	Renderable getHoverInfo(YamlPathSegment lastSegment);

	Renderable getValueHoverInfo(YamlDocument doc, DocumentRegion documentRegion);

	YamlDocument getDocument();

	default List<Location> getDefinitionsForPropertyKey() {
		return ImmutableList.of();
	}

	default List<Location> getDefinitionsForPropertyValue(DocumentRegion valueRegion) {
		return ImmutableList.of();
	}
}
