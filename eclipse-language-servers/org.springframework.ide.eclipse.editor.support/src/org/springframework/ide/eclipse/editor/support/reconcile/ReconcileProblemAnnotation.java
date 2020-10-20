/*******************************************************************************
 * Copyright (c) 2006, 2008, 20014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Kris De Volder - copied from SpellingAnnotation to become
 *     					'SpringPropertyAnnotation'.
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;

/**
 * Spelling annotation.
 *
 * @since 3.3
 */
@SuppressWarnings("restriction")
public class ReconcileProblemAnnotation extends Annotation implements IQuickFixableAnnotation {

	/** Annotation type for error and warnings*/
	public static final String ERROR_ANNOTATION_TYPE = org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE;
	public static final String WARNING_ANNOTATION_TYPE = org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE;
		//Could use our own annotation type (but then we also have to declare it somehow to make it show error style marker)

	public static final Set<String> TYPES = new HashSet<String>();
	static {
		TYPES.add(ERROR_ANNOTATION_TYPE);
		TYPES.add(WARNING_ANNOTATION_TYPE);
	}

	public static String getAnnotationType(ProblemSeverity severity) {
		switch (severity) {
		case ERROR:
			return ERROR_ANNOTATION_TYPE;
		case WARNING:
			return WARNING_ANNOTATION_TYPE;
		case IGNORE:
			return null;
		default:
			throw new IllegalStateException("Bug: Missing switch case!");
		}
	}

	private ReconcileProblem fProblem;

	/**
	 * Creates a new annotation of given type.
	 */
	public ReconcileProblemAnnotation(String annotationType, ReconcileProblem problem) {
		super(annotationType, false, problem.getMessage());
		fProblem = problem;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable()
	 */
	public boolean isQuickFixable() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixableStateSet()
	 */
	public boolean isQuickFixableStateSet() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#setQuickFixable(boolean)
	 */
	public void setQuickFixable(boolean state) {
		// always true
	}

	public ReconcileProblem getSpringPropertyProblem() {
		return fProblem;
	}

}
