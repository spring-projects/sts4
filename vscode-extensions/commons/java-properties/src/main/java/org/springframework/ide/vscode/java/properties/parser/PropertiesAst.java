/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.java.properties.parser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Very basic AST for Java Properties to keep comments and key value pairs
 * 
 * @author Alex Boyko
 *
 */
public final class PropertiesAst {
	
	private List<Node> nodes;
	
	public PropertiesAst(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * Retrieves all AST nodes
	 * @return List of AST nodes sorted by line number
	 */
	public List<? extends Node> getAllNodes() {
		return nodes;
	}
	
	/**
	 * Retrieves AST nodes of specific type
	 * @param clazz Type of AST nodes
	 * @return List of AST nodes of specific type sorted by line number
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getNodes(Class<T> clazz) {
		List<Node> l = nodes.stream().filter(line -> {
			return clazz.isAssignableFrom(line.getClass());
		}).collect(Collectors.toList());
		return (List<T>) l;
	}
	
	/**
	 * Java Properties AST node
	 */
	public interface Node {

		/**
		 * Offset index of a node in the document 
		 * @return Offset index
		 */
		int getOffset();
		
		/**
		 * Number of characters a node occupies in the document
		 * @return Number of characters
		 */
		int getLength();
		
	}
	
	/**
	 * AST node for comment 
	 */
	public interface Comment extends Node {
		
	}

	/**
	 * AST node for property key and property value pair  
	 */
	public interface KeyValuePair extends Node {
		
		/**
		 * AST node for key
		 * @return Node for key
		 */
		Key getKey();
		
		/**
		 * AST node for value
		 * @return Node for value
		 */
		Value getValue();

	}
	
	/**
	 * AST node for property key 
	 */
	public interface Key extends Node {
		
		/**
		 * Decode possibly encoded property name
		 * @return Decoded property name
		 */
		String decode();

	}

	/**
	 * AST node for property value
	 */
	public interface Value extends Node {
		
		/**
		 * Decode possibly encoded property value
		 * @return Decoded property value
		 */
		String decode();

	}
	
}
