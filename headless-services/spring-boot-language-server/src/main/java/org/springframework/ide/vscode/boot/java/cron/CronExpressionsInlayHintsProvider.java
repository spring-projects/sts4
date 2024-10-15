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

import java.util.Locale;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.JdtInlayHintsProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.scheduling.support.CronExpression;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static com.cronutils.model.CronType.SPRING;

public class CronExpressionsInlayHintsProvider implements JdtInlayHintsProvider {

	protected static Logger logger = LoggerFactory.getLogger(CronExpressionsInlayHintsProvider.class);

	private static final String SCHEDULED = "Scheduled";

	private final BootJavaConfig config;

	public record EmbeddedCronExpression(Expression expression, String text, int offset) {
	};

	public CronExpressionsInlayHintsProvider(BootJavaConfig config) {
		this.config = config;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return config.isCronInlayHintsEnabled();
	}

	@Override
	public ASTVisitor getInlayHintsComputer(IJavaProject project, TextDocument doc, CompilationUnit cu,
			Collector<InlayHint> collector) {
		return new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation node) {
				EmbeddedCronExpression cron = extractCronExpression(node);
				if (cron != null) {
					processCron(project, doc, collector, cron, node);
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				EmbeddedCronExpression cron = extractCronExpression(node);
				if (cron != null) {
					processCron(project, doc, collector, cron, node);
				}
				return super.visit(node);
			}

		};
	}

	private void processCron(IJavaProject project, TextDocument doc, Collector<InlayHint> collector,
			EmbeddedCronExpression cronExp, Annotation node) {
		boolean isValidExpression = CronExpression.isValidExpression(cronExp.text());

		try {
			if (isValidExpression) {
				CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(SPRING);
			    CronParser parser = new CronParser(cronDefinition);
			    CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
			    String cronDescription = descriptor.describe(parser.parse(cronExp.text().toUpperCase()));
			    
				InlayHint hint = new InlayHint();
				hint.setKind(InlayHintKind.Type);
				hint.setLabel(Either.forLeft(cronDescription));
				hint.setTooltip(cronDescription);
				hint.setPaddingLeft(true);
				hint.setPaddingRight(true);
				hint.setPosition(doc.toPosition(node.getStartPosition() + node.getLength()));
				collector.accept(hint);
			}
		} catch (Exception e) {
			// ignore
		}
	}

	public static EmbeddedCronExpression extractCronExpression(SingleMemberAnnotation a) {
		if (isScheduledAnnotation(a)) {
			EmbeddedCronExpression expression = extractEmbeddedExpression(a.getValue(), a);
			return expression == null ? null
					: new EmbeddedCronExpression(expression.expression(), expression.text(), expression.offset());
		}
		return null;
	}

	public static EmbeddedCronExpression extractCronExpression(NormalAnnotation a) {
		Expression cronExpression = null;
		if (isScheduledAnnotation(a)) {
			for (Object value : a.values()) {
				if (value instanceof MemberValuePair) {
					MemberValuePair pair = (MemberValuePair) value;
					String name = pair.getName().getFullyQualifiedName();
					if ("cron".equals(name)) {
						cronExpression = pair.getValue();
						break;
					}
				}
			}
		}
		if (cronExpression != null) {
			EmbeddedCronExpression e = extractEmbeddedExpression(cronExpression, a);
			if (e != null) {
				return new EmbeddedCronExpression(e.expression(), e.text(), e.offset());
			}
		}
		return null;
	}

	public static EmbeddedCronExpression extractEmbeddedExpression(Expression valueExp, Annotation node) {
		String text = null;
		int offset = 0;
		if (valueExp instanceof StringLiteral sl) {
			text = sl.getEscapedValue();
			text = text.substring(1, text.length() - 1);
			offset = sl.getStartPosition() + 1; // +1 to skip over opening "
		} else if (valueExp instanceof TextBlock tb) {
			text = tb.getEscapedValue();
			text = text.substring(3, text.length() - 3).trim();
			offset = tb.getStartPosition() + 3; // +3 to skip over opening """
		}
		return text == null ? null : new EmbeddedCronExpression(valueExp, text, offset);
	}

	static boolean isScheduledAnnotation(Annotation a) {
		return Annotations.SCHEDULED.equals(a.getTypeName().getFullyQualifiedName())
				|| SCHEDULED.equals(a.getTypeName().getFullyQualifiedName());
	}
}
