/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

/**
 * Indicates that a reconciler or indexer requires the full AST (including method bodies)
 * to do its work accordingly.
 */
public class RequiredCompleteAstException extends RuntimeException {
	private static final long serialVersionUID = -3422411902406544588L;
}
