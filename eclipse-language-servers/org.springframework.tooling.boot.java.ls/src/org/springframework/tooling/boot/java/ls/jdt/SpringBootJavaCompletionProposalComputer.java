/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.java.ls.jdt;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class SpringBootJavaCompletionProposalComputer implements IJavaCompletionProposalComputer {

	private LSContentAssistProcessor lsContentAssistProcessor;

	public SpringBootJavaCompletionProposalComputer() {
		lsContentAssistProcessor = new LSContentAssistProcessor();
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		System.out.println("Spring Boot Java Completion Proposal - compute completion proposals");

		ICompletionProposal[] proposals = lsContentAssistProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset());
		return Arrays.asList(proposals);
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		IContextInformation[] contextInformation = lsContentAssistProcessor.computeContextInformation(context.getViewer(), context.getInvocationOffset());
		return Arrays.asList(contextInformation);
	}

	@Override
	public String getErrorMessage() {
		return lsContentAssistProcessor.getErrorMessage();
	}

	@Override
	public void sessionEnded() {
	}

}
