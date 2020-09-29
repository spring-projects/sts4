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
package org.springframework.ide.eclipse.editor.support.yaml.completions;

import java.util.Collection;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.DocumentRegion;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlNavigable;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;

/**
 * @author Kris De Volder
 */
public interface YamlAssistContext extends YamlNavigable<YamlAssistContext> {
	Collection<ICompletionProposal> getCompletions(YamlDocument doc, SNode node, int offset) throws Exception;

	//TODO: conceptually... the right thing would be to only implement the second of these
	// two methods and get rid of the first one.
	HoverInfo getHoverInfo();
	HoverInfo getHoverInfo(YamlPathSegment lastSegment);

	HoverInfo getValueHoverInfo(YamlDocument doc, DocumentRegion documentRegion);
}
