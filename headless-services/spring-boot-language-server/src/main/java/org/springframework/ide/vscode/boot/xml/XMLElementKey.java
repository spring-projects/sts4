/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

/**
 * @author Martin Lippert
 */
public class XMLElementKey {

	private final String namespaceURI;
	private final String elementName;
	private final String attributeName;
	private final String parentNodeName;

	public XMLElementKey(String namespaceURI, String parentNodeName, String elementName, String attributeName) {
		super();
		this.namespaceURI = namespaceURI;
		this.parentNodeName = parentNodeName;
		this.elementName = elementName;
		this.attributeName = attributeName;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public String getParentNodeName() {
		return parentNodeName;
	}

	public String getElementName() {
		return elementName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
		result = prime * result + ((namespaceURI == null) ? 0 : namespaceURI.hashCode());
		result = prime * result + ((parentNodeName == null) ? 0 : parentNodeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMLElementKey other = (XMLElementKey) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (elementName == null) {
			if (other.elementName != null)
				return false;
		} else if (!elementName.equals(other.elementName))
			return false;
		if (namespaceURI == null) {
			if (other.namespaceURI != null)
				return false;
		} else if (!namespaceURI.equals(other.namespaceURI))
			return false;
		if (parentNodeName == null) {
			if (other.parentNodeName != null)
				return false;
		} else if (!parentNodeName.equals(other.parentNodeName))
			return false;
		return true;
	}

}
