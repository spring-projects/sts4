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
package org.springframework.tooling.jdt.ls.commons.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteData;

/**
 * @author Martin Lippert
 */
public class JavaCodeCompletionProposalCollector extends CompletionRequestor {

	private final boolean includeTypes;
	private final boolean includePackages;
	private final List<JavaCodeCompleteData> proposals;

	public JavaCodeCompletionProposalCollector(boolean includeTypes, boolean includePackages) {
		this.includeTypes = includeTypes;
		this.includePackages = includePackages;
		this.proposals = new ArrayList<>();
	}

	@Override
	public void accept(CompletionProposal proposal) {
		if (includeTypes && proposal.getKind() == CompletionProposal.TYPE_REF) {
			if (!JavaCodeCompletionUtils.CLASS_NAME.equals(String.valueOf(proposal.getCompletion()))) {
				addTypeProposal(proposal);
			}
		}
		else if (includePackages && proposal.getKind() == CompletionProposal.PACKAGE_REF) {
			addPackageProposal(proposal);
		}
	}

	public List<JavaCodeCompleteData> getProposals() {
		return proposals;
	}

	private void addPackageProposal(CompletionProposal proposal) {
		JavaCodeCompleteData proposalData = new JavaCodeCompleteData();
		
		proposalData.setKind(JavaCodeCompleteData.PACKAGE_PROPOSAL);
		proposalData.setRelevance(proposal.getRelevance());
		proposalData.setFullyQualifiedName(String.valueOf(proposal.getDeclarationSignature()));
		
		this.proposals.add(proposalData);
	}

	private void addTypeProposal(CompletionProposal proposal) {
		JavaCodeCompleteData proposalData = new JavaCodeCompleteData();
		
		if (Flags.isInterface(proposal.getFlags())) {
			proposalData.setKind(JavaCodeCompleteData.INTERFACE_PROPOSAL);
		}
		else if (Flags.isEnum(proposal.getFlags())) {
			proposalData.setKind(JavaCodeCompleteData.ENUM_PROPOSAL);
		}
		else {
			proposalData.setKind(JavaCodeCompleteData.CLASS_PROPOSAL);
		}
		proposalData.setRelevance(proposal.getRelevance());
		
		String fullyQualifiedName = String.valueOf(Signature.toCharArray(proposal.getSignature()));
		proposalData.setFullyQualifiedName(fullyQualifiedName);
		
		this.proposals.add(proposalData);
	}

}
