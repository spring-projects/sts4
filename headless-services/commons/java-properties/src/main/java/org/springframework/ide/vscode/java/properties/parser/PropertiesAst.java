/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
	public List<Node> getAllNodes() {
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
	 * Find node in AST corresponding to offset position
	 * @param offset Position in the text 
	 * @return AST node corresponding to the offset position
	 */
	public Node findNode(int offset) {
		return findNode(nodes, offset, 0, nodes.size() - 1);
	}
	
	private Node findNode(List<? extends Node> nodes, int offset, int start, int end) {
		if (nodes == null) {
			return null;
		}
		if (start == end) {
			Node node = nodes.get(start);
			if (node.getOffset() <= offset && offset <= node.getOffset() + node.getLength()) {
				Node found = findChildNode(node, offset);
				return found == null ? node : found;
			} else {
				return null;
			}
		} else if (start < end ) {
			int pivotIndex = (start + end) / 2;
			Node node = nodes.get(pivotIndex);
			if (node.getOffset() > offset) {
				return findNode(nodes, offset, start, pivotIndex - 1);
			} else if (offset > node.getOffset() + node.getLength()) {
				return findNode(nodes, offset, pivotIndex + 1, end);
			} else {
				Node found = findChildNode(node, offset);
				return found == null ? node : found;
			}
		} else {
			return null;
		}
	}
	
	private Node findChildNode(Node node, int offset) {
		if (node.getChildren() == null) {
			return null;
		} else {
			return findNode(node.getChildren(), offset, 0, node.getChildren().size() - 1);
		}
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
		
		/**
		 * Node's parent
		 * @return parent node
		 */
		Node getParent();
		
		/**
		 * Node's children
		 * @return children nodes
		 */
		List<? extends Node> getChildren();
		
	}
	
	/**
	 * AST node for comment 
	 */
	public interface Comment extends Node {
		
	}
	
	/**
	 * AST node for empty line 
	 */
	public interface EmptyLine extends Node {
		
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
		
		KeyValuePair getParent();

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

		KeyValuePair getParent();
	}
	
}
