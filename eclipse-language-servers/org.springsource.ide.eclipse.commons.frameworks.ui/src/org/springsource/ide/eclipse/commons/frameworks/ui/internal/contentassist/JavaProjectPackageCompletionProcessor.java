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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * This may be a temporary content assist solution for STS. It uses the same
 * deprecated infrastructure as the Java New Type wizard. However, if the Java
 * new type wizard changes to platform content assist, then this completion
 * processor should be deprecated, and STS should adopt the newer content assist
 * framework.
 * <p>
 * This package content assist only works PER single java project, and will ONLY find
 * package fragments in a given Java project for package fragment roots that are
 * of CPE_SOURCE kind. If this needs to be changed, the CPE_SOURCE check should
 * be removed in the code below
 * </p>
 * @author Nieraj Singh
 */
public class JavaProjectPackageCompletionProcessor implements
		IProjectContentAssistProcessor, ISubjectControlContentAssistProcessor {

	private CompletionProposalComparator proposalComparator;
	private ILabelProvider labelProvider;
	private IJavaProject project;

	private char[] proposalAutoActivationSet;

	/**
	 * Creates a <code>JavaProjectPackageCompletionProcessor</code>. The
	 * completion context must be set via {@link #setProject(IJavaProject)}.
	 */
	public JavaProjectPackageCompletionProcessor() {
		this(new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_SMALL_ICONS));
	}

	/**
	 * Creates a <code>JavaPackageCompletionProcessor</code>. The Processor uses
	 * the given <code>ILabelProvider</code> to show text and icons for the
	 * possible completions.
	 * 
	 * @param labelProvider
	 *           Used for the popups.
	 */
	public JavaProjectPackageCompletionProcessor(ILabelProvider labelProvider) {
		proposalComparator = new CompletionProposalComparator();
		this.labelProvider = labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.springsource.sts.frameworks.ui.internal.contentassist.
	 * IProjectContentAssistProcessor#getProject()
	 */
	public IJavaProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.springsource.sts.frameworks.ui.internal.contentassist.
	 * IProjectContentAssistProcessor
	 * #setProject(org.eclipse.jdt.core.IJavaProject)
	 */
	public void setProject(IJavaProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int documentOffset) {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		if (proposalAutoActivationSet == null) {
			IPreferenceStore preferenceStore = JavaPlugin.getDefault()
					.getPreferenceStore();
			String triggers = preferenceStore
					.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
			proposalAutoActivationSet = triggers.toCharArray();
		}
		return proposalAutoActivationSet;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage
	 * ()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#
	 * getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeContextInformation(
	 * IContentAssistSubjectControl, int)
	 */
	public IContextInformation[] computeContextInformation(
			IContentAssistSubjectControl contentAssistSubjectControl,
			int documentOffset) {
		return null;
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeCompletionProposals(
	 * IContentAssistSubjectControl, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(
			IContentAssistSubjectControl contentAssistSubjectControl,
			int documentOffset) {
		if (project == null) {
			return null;
		}
		String input = contentAssistSubjectControl.getDocument().get();
		ICompletionProposal[] foundProposals = null;

		IPackageFragmentRoot[] roots;
		try {
			roots = project.getAllPackageFragmentRoots();
			if (roots == null) {
				return null;
			}
		} catch (JavaModelException e1) {
			return null;
		}

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String prefix = input.substring(0, documentOffset);
		for (IPackageFragmentRoot root : roots) {
			try {
				// To filter out all other package fragments from dependencies,
				// ONLY check for source root types. A similar thing is done
				// for the Java new type wizard. If this needs to be changed
				// comment out the CPE_SOURCE check below.
				IClasspathEntry entry = root.getRawClasspathEntry();
				if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
					continue;
				}
				IJavaElement[] children = root.getChildren();
				for (IJavaElement child : children) {
					// Check for duplicates
					if (child instanceof IPackageFragment) {
						IPackageFragment packageFragment = (IPackageFragment) child;
						String packName = packageFragment.getElementName();
						if (packName.startsWith(prefix)) {
							Image image = labelProvider
									.getImage(packageFragment);
							JavaCompletionProposal proposal = new JavaCompletionProposal(
									packName, 0, input.length(), image,
									labelProvider.getText(packageFragment), 0);
							proposals.add(proposal);
						}
					}

				}
			} catch (JavaModelException e) {
				// no proposals for this package fragment root, skip to next
			}
		}

		foundProposals = (ICompletionProposal[]) proposals
				.toArray(new ICompletionProposal[proposals.size()]);

		Arrays.sort(foundProposals, proposalComparator);
		return foundProposals;
	}

}
