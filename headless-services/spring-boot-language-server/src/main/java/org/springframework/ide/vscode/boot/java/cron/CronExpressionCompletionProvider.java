/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.cron;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeProposal;
import org.springframework.ide.vscode.commons.java.IJavaProject;

public class CronExpressionCompletionProvider implements AnnotationAttributeCompletionProvider {
	
	 private static final List<AnnotationAttributeProposal> CRON_EXPRESSIONS_MAP = List.of(
		        new AnnotationAttributeProposal("0 0 * * * *", "every hour"),
		        new AnnotationAttributeProposal("0 0 * * * 1-5", "every hour every day between Monday and Friday"),
		        new AnnotationAttributeProposal("0 * * * * *", "every minute"),
		        new AnnotationAttributeProposal("0 */5 * * * *", "every 5 minutes"),
		        new AnnotationAttributeProposal("0 0 */6 * * *", "every 6 hours at minute 0"),
		        new AnnotationAttributeProposal("0 0 * * * SUN", "every hour at Sunday day"),
		        new AnnotationAttributeProposal("0 0 0 * * *", "at 00:00"),
		        new AnnotationAttributeProposal("0 0 0 * * SAT,SUN", "at 00:00 on Saturday and Sunday"),
		        new AnnotationAttributeProposal("0 0 0 * * 6,0", "at 00:00 at Saturday and Sunday days"),
		        new AnnotationAttributeProposal("0 0 0 1-7 * SUN", "at 00:00 every day between 1 and 7 at Sunday day"),
		        new AnnotationAttributeProposal("0 0 0 1 * *", "at 00:00 at 1 day"),
		        new AnnotationAttributeProposal("0 0 0 1 1 *", "at 00:00 at 1 day at January month"),
		        new AnnotationAttributeProposal("0 0 8-18 * * *", "every hour between 8 and 18"),
		        new AnnotationAttributeProposal("0 0 9 * * MON", "at 09:00 at Monday day"),
		        new AnnotationAttributeProposal("0 0 10 * * *", "at 10:00"),
		        new AnnotationAttributeProposal("0 30 9 * JAN MON", "at 09:30 at January month at Monday day"),
		        new AnnotationAttributeProposal("10 * * * * *", "every minute at second 10"),
		        new AnnotationAttributeProposal("0 0 8-10 * * *", "every hour between 8 and 10"),
		        new AnnotationAttributeProposal("0 0/30 8-10 * * *", "every 30 minutes every hour between 8 and 10"),
		        new AnnotationAttributeProposal("0 0 0 L * *", " at 00:00 last day of month"),
		        new AnnotationAttributeProposal("0 0 0 1W * *", "at 00:00 the nearest weekday to the 1 of the month"),
		        new AnnotationAttributeProposal("0 0 0 * * THUL", "at 00:00 last Thursday of every month"),
		        new AnnotationAttributeProposal("0 0 0 ? * 5#2", "at 00:00 Friday 2 of every month"),
		        new AnnotationAttributeProposal("0 0 0 ? * MON#1", "at 00:00 Monday 1 of every month")
			 );

    @Override
    public List<AnnotationAttributeProposal> getCompletionCandidates(IJavaProject project, ASTNode node) {
        return CRON_EXPRESSIONS_MAP;
    }
}
