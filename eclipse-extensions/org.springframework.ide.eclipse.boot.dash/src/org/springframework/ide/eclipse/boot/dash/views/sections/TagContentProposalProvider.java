/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

/**
 * Provider for tags content proposals
 * 
 * @author Alex Boyko
 *
 */
public class TagContentProposalProvider implements IContentProposalProvider {
	
	private BootDashViewModel model;

	public TagContentProposalProvider(BootDashViewModel model) {
		this.model = model;
	}
	
	private Set<String> getAllTags() {
		HashSet<String> tags = new HashSet<String>();
		for (BootDashModel sectionModel : model.getSectionModels().getValue()) {
			for (BootDashElement element : sectionModel.getElements().getValue()) {
				tags.addAll(element.getTags());
			}
		}
		return tags;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		Set<String> allTags = getAllTags();
		
		int startPosition = getCurrentTagStartPosition(contents, position);
		int endPosition = getCurrentTagEndPosition(contents, position);
		String pattern = contents.substring(startPosition, position);
		String tagUnderCursor = contents.substring(startPosition, endPosition);
		HashSet<String> currentTags = new HashSet<String>(Arrays.asList(TagUtils.parseTags(contents)));
		if (pattern.equals(tagUnderCursor)) {
			currentTags.remove(tagUnderCursor);
		}
		allTags.removeAll(currentTags);

		List<IContentProposal> proposals = new ArrayList<IContentProposal>(allTags.size());
		for (String tag : allTags) {
			if (FuzzyMatcher.matchScore(pattern, tag) != 0) {
				proposals.add(tagProposal(tag, applyProposal(contents, tag, startPosition, position), startPosition + tag.length()));
			}
		}
		return proposals.toArray(new IContentProposal[proposals.size()]);
	}
	
	private static int getCurrentTagStartPosition(String content, int position) {
		int i = position - 1;
		for (; i >= 0 && content.charAt(i) != TagUtils.SEPARATOR_SYMBOL; i--);
		i++;
		for (; i < content.length() && Pattern.matches("\\s", "" + content.charAt(i)); i++);
		return i;
	}
	
	private static int getCurrentTagEndPosition(String content, int position) {
		int i = position;
		for (; i < content.length() && content.charAt(i) != TagUtils.SEPARATOR_SYMBOL; i++);
		for (; i > position && Pattern.matches("\\s", "" + content.charAt(i - 1)); i--);
		return i;
	}
	
	private static String applyProposal(String content, String proposal, int start, int end) {
		StringBuilder sb = new StringBuilder(start + proposal.length() + content.length() - end);
		sb.append(content.substring(0, start));
		sb.append(proposal);
		sb.append(content.substring(end));
		return sb.toString();
	}
	
	private IContentProposal tagProposal(final String label, final String newContent, final int cursorPosition) {
		return new IContentProposal() {

			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public int getCursorPosition() {
				return cursorPosition;
			}

			@Override
			public String getContent() {
				return newContent;
			}
			
		};
	}

}
