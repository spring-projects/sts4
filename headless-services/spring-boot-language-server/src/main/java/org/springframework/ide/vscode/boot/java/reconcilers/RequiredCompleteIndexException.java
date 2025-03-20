/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

/**
 * Indicates that a reconciler relies on the index to be complete before it can
 * reconcile a file. As a result, the corresponding file will be re-parsed and re-reconciled
 * once the index is complete.
 * 
 * A reconciler should throw this exception only in case it really requires
 * access to the index and check other AST-related information first to avoid
 * re-reconciling too many files when it is not really necessary.
 * 
 * For example the reconciler that checks BeanRegistrar implementations against
 * Import annotations in the index should only throw this exception if the class
 * is indeed an implementation of BeanRegistrar.
 */
public class RequiredCompleteIndexException extends RuntimeException {
	private static final long serialVersionUID = -6155363860106363727L;
}
