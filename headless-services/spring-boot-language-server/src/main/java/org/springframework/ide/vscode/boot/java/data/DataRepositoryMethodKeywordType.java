/*******************************************************************************
 * Copyright (c) 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

/**
 * Types of predicate keywords Spring JPA repository method names
 * @author danthe1st
 */
enum DataRepositoryMethodKeywordType {
	/**
	 * A keyword that terminates an expression.
	 *
	 * e.g. {@code isTrue} in {@code findBySomeBooleanIsTrue}
	 */
	TERMINATE_EXPRESSION,
	/**
	 * An operator combining two conditions.
	 *
	 * e.g. {@code And} in {@code findBySomeAttributeAndAnotherAttribute}
	 */
	COMBINE_CONDITIONS,
	/**
	 * A keyword requiring an expression on both sides or an expression on one side and a parameter.
	 *
	 * e.g. {@code Equals} in {@code findBySomeAttributeEquals} or {@code findBySomeAttributeEqualsAnotherAttribute}
	 */
	COMPARE,
	/**
	 * Keywords that can be ignored for content assist.
	 *
	 * e.g. {@code Not} in {@code findByNotSomeBooleanAttribute}
	 */
	IGNORE;
}