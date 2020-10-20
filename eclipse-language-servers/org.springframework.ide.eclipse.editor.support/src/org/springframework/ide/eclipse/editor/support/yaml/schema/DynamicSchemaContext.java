/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.schema;

import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;

import com.google.common.collect.ImmutableSet;

/**
 * Exposes some 'dynamic' contextual information to schema implementations.
 * <p>
 * This is necessary to enable the impementation of schema features where one
 * part of a yaml structure depends details of another part. A typical example
 * could be 'type' property, so that different values assigned to that property
 * change the structure that is expected for the rest of the node.
 *
 * @author Kris De Volder
 */
public interface DynamicSchemaContext {

	DynamicSchemaContext NULL = new DynamicSchemaContext() {

		@Override
		public Set<String> getDefinedProperties() {
			return ImmutableSet.of();
		}

		@Override
		public IDocument getDocument() {
			return null;
		}

		@Override
		public YamlPath getPath() {
			return null;
		}

	};

	/**
	 * Returns the enitre AST of the current document. May be null if the AST is not
	 * available (e.g. because of parsing errors)
	 */
	default YamlFileAST getAST() { return null; }

	/**
	 * Returns the set of property names that are already defined in the current context.
	 * <p>
	 * In Content assist scenarios this is only a 'best effort' result. An empty set may
	 * be returned because either no properties are yet defined, or because the sloppy parser
	 * can not quite make out the yaml structure because yaml text is incomplete or too complex
	 * for its analysis.
	 * <p>
	 * In a validation scenario however, the returned information should precisely reflect what
	 * properties are defined in the surrounding object.
	 */
	Set<String> getDefinedProperties();

	/**
	 * Returns the IDocument the current context is in. This allows for some 'schemas' to have
	 * arbitrarily complex analysis of anyhting in the IDocument or even documents related
	 * to it based on its uri.
	 */
	IDocument getDocument();

	/**
	 * Returns the yamlpath leading to the current node.
	 */
	YamlPath getPath();

}
