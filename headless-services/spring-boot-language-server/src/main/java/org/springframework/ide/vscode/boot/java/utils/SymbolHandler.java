/*******************************************************************************
 * Copyright (c) 2019, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

/**
 * @author Martin Lippert
 */
public interface SymbolHandler {

	void addSymbols(IJavaProject project, String docURI, EnhancedSymbolInformation[] enhancedSymbols, Bean[] beanDefinitions);
	void addSymbols(IJavaProject project, EnhancedSymbolInformation[] enhancedSymbols, Bean[] beanDefinitions);

	void removeSymbols(IJavaProject project, String docURI);

}
