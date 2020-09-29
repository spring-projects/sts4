/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import static org.springframework.ide.eclipse.boot.wizard.BootWizardImages.BOOT_SMALL_ICON;
import static org.springframework.ide.eclipse.boot.wizard.BootWizardImages.getImage;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizard;
import org.springframework.ide.eclipse.editor.support.util.DocumentUtil;

import com.google.common.collect.ImmutableList;

public class BootPomCompletionProposalComputer implements ICompletionProposalComputer {

	Image BOOT_ICN = getImage(BOOT_SMALL_ICON);

	public BootPomCompletionProposalComputer() {
	}

	@Override
	public void sessionStarted() {
	}

	@Override
	public List computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		IProject project = DocumentUtil.getProject(context.getDocument());
		if (BootPropertyTester.isBootProject(project)) {
			return ImmutableList.of(editStartersCompletion(project));
		}
		return ImmutableList.of();
	}

	private ICompletionProposal editStartersCompletion(IProject project) {
		return new ICompletionProposal() {

			@Override
			public void apply(IDocument document) {
				try {
					AddStartersWizard.openFor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), project);
				} catch (CoreException e) {
					Log.log(e);
				}
			}

			@Override
			public Point getSelection(IDocument document) {
				return null;
			}

			@Override
			public String getAdditionalProposalInfo() {
				return "Open 'Add Spring Starters' Dialog";
			}

			@Override
			public String getDisplayString() {
				return "Add Starters...";
			}

			@Override
			public Image getImage() {
				return BOOT_ICN;
			}

			@Override
			public IContextInformation getContextInformation() {
				return null;
			}
		};
	}

	@Override
	public List computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded() {
	}

}
