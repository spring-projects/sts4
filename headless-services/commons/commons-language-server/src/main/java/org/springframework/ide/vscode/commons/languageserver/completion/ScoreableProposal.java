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

package org.springframework.ide.vscode.commons.languageserver.completion;

import java.util.Comparator;

import org.springframework.ide.vscode.commons.util.Assert;

public abstract class ScoreableProposal implements ICompletionProposal {

	public static final double DEEMP_EXISTS = 0.1;
	public static final double DEEMP_DEPRECATION = 0.2;
	public static final double DEEMP_NEXT_CONTEXT = 0.0;
	public static final double DEEMP_INDENTED_PROPOSAL = 0.4;
	public static final double DEEMP_DASH_PROPOSAL = 0.6;
	public static final double DEEMP_DEDENTED_PROPOSAL = 0.8;

	private static final double DEEMP_VALUE = 10_000; // should be large enough to move deemphasized stuff to bottom of list.

	private double deemphasizedBy = 0.0;

	/**
	 * A sorter suitable for sorting ScoreableProposals based on their score.
	 */
	public static final Comparator<ICompletionProposal> COMPARATOR = new Comparator<ICompletionProposal>() {
		@Override
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			if (p1 instanceof ScoreableProposal && p2 instanceof ScoreableProposal) {
				double s1 = ((ScoreableProposal)p1).getScore();
				double s2 = ((ScoreableProposal)p2).getScore();
				if (s1==s2) {
					String name1 = ((ScoreableProposal)p1).getLabel();
					String name2 = ((ScoreableProposal)p2).getLabel();
					return name1.compareTo(name2);
				} else {
					return Double.compare(s2, s1);
				}
			}
			if (p1 instanceof ScoreableProposal) {
				return -1;
			}
			if (p2 instanceof ScoreableProposal) {
				return +1;
			}
			return p1.getLabel().compareTo(p2.getLabel());
		}
	};
	public abstract double getBaseScore();
	public final double getScore() {
		return getBaseScore() - deemphasizedBy;
	}
	@Override
	public ScoreableProposal deemphasize(double howmuch) {
		Assert.isLegal(howmuch>=0.0);
		deemphasizedBy+= howmuch*DEEMP_VALUE;
		return this;
	}
	public boolean isDeemphasized() {
		return deemphasizedBy > 0;
	}

	@Override
	public String toString() {
		return getLabel();
	}

}