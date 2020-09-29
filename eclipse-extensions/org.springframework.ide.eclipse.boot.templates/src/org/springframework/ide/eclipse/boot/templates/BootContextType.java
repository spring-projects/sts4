/*******************************************************************************
 * Copyright (c) 2007, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pivotal Inc. - copied from JDT and adapted for STS
 *******************************************************************************/

package org.springframework.ide.eclipse.boot.templates;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitContext;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;

/**
 * The context type for templates inside 'Spring Boot' code.
 * The same class is used for several context types:
 * <dl>
 * <li>templates for all Java code locations</li>
 * <li>templates for member locations</li>
 * <li>templates for statement locations</li>
 * </dl>
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public class BootContextType extends AbstractJavaContextType {

	private boolean initialized = false;

	public BootContextType() {
		super();
	}

	/**
	 * The context type id for templates working on all Java code locations in Boot projects
	 */
	public static final String ID_ALL= "boot"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on member locations in Boot projects
	 */
	public static final String ID_MEMBERS= "boot-members"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on statement locations in Boot projects
	 */
	public static final String ID_STATEMENTS= "boot-statements"; //$NON-NLS-1$

	@Override
	public java.util.Iterator resolvers() {
		ensureInitialized();
		return super.resolvers();
	}

	private synchronized void ensureInitialized() {
		if (!initialized) {
			initialized = true;
			ContributionContextTypeRegistry registry = (ContributionContextTypeRegistry) JavaPlugin.getDefault().getTemplateContextRegistry();
			String superId = "java"+getId().substring(ID_ALL.length());
			TemplateContextType superType = registry.getContextType(superId);
			copyResolvers(superType);
		}
	}

	/**
	 * Copy resolvers from one template type to another.
	 */
	private void copyResolvers(TemplateContextType parent) {
		java.util.Iterator<TemplateVariableResolver> iter= parent.resolvers();
		while (iter.hasNext())
			this.addResolver(iter.next());
	}

	private void doInitializeContext(BootJavaContext context) {
		ensureInitialized();
		if (!getId().equals(BootContextType.ID_ALL)) { // a specific context must also allow the templates that work everywhere
			context.addCompatibleContextType(BootContextType.ID_ALL);
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, int, int, org.eclipse.jdt.core.ICompilationUnit)
	 */
	@Override
	public CompilationUnitContext createContext(IDocument document, int offset, int length, ICompilationUnit compilationUnit) {
		BootJavaContext javaContext= new BootJavaContext(this, document, offset, length, compilationUnit);
		doInitializeContext(javaContext);
		return javaContext;
	}

	/*
	 * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position, org.eclipse.jdt.core.ICompilationUnit)
	 */
	@Override
	public CompilationUnitContext createContext(IDocument document, Position completionPosition, ICompilationUnit compilationUnit) {
		BootJavaContext javaContext= new BootJavaContext(this, document, completionPosition, compilationUnit);
		doInitializeContext(javaContext);
		return javaContext;
	}

}
