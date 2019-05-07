/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.IAsyncCompletionProposalComputer;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;
import org.springframework.tooling.boot.ls.Constants;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class XMLContentAssistProposalComputer implements IAsyncCompletionProposalComputer {

	private static TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
	private static long TIMEOUT_LENGTH = 2000;

	private LSContentAssistProcessor lsContentAssistProcessor;

	public XMLContentAssistProposalComputer() {
		lsContentAssistProcessor = new LSContentAssistProcessor();
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		if (!BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS)) {
			return Collections.emptyList();
		}
		
		CompletableFuture<ICompletionProposal[]> future = CompletableFuture.supplyAsync(() -> {
			return lsContentAssistProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset());
		});
		
		try {
			return Arrays.asList(future.get(TIMEOUT_LENGTH, TIMEOUT_UNIT));
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	@Override
	public List<IContextInformation> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		if (!BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_SUPPORT_SPRING_XML_CONFIGS)
				|| !BootLanguageServerPlugin.getDefault().getPreferenceStore().getBoolean(Constants.PREF_XML_CONFIGS_CONTENT_ASSIST)) {
			return Collections.emptyList();
		}

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

	@Override
	public void sessionStarted() {
	}

}
