/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.pom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IDocumentRange;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;
import org.springsource.ide.eclipse.commons.frameworks.core.util.DocumentRegion;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.xml.sax.Locator;

public class DomStructureComparable implements IStructureComparator, IDocumentRange, ITypedElement {
	
	final private DomType type;
	private Position range;
	private IDocument document;
	private DomStructureComparable[] children;
	private Object id;
	private String name;
	private DomStructureComparable parent;

	public enum DomType {
		ROOT,
		ELEMENT,
		ATTRIBUTE,
		TEXT
	}
	
	public static class Builder {
		
		final public DomType type;
		int start;
		int end;
		final public IDocument document;
		Builder parent;
		
		List<DomStructureComparable> children = new ArrayList<>();
		String name;
		
		public Builder(DomType type, IDocument document) {
			this.type = type;
			this.document = document;
		}

		public void start(Locator locator) {
			try {
				int offset = document.getLineOffset(locator.getLineNumber() - 1) + locator.getColumnNumber() - 1;
				start = adjustStartPositionForElement(document, offset);
			} catch (BadLocationException e) {
				throw ExceptionUtil.unchecked(e);
			}
		}
		
		public void end(Locator locator) {
			if (locator.getLineNumber() < 0) {
				end = document.getLength();				
			} else {
				try {
					int offset  = document.getLineOffset(locator.getLineNumber() - 1) + locator.getColumnNumber() - 1;
					end = adjustEndPositionForElement(document, offset);
				} catch (BadLocationException e) {
					throw ExceptionUtil.unchecked(e);
				}
			}
		}

		public Builder parent(Builder parent) {
			this.parent = parent;
			return this;
		}

		public void addChild(DomStructureComparable child) {
			this.children.add(child);
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public DomStructureComparable build(Object id) {
			return new DomStructureComparable(
					id,
					type,
					name,
					document,
					new Position(start, end -start),
					children.toArray(new DomStructureComparable[children.size()]));
		}

	}
	
	public static DomStructureComparable.Builder createRoot(IDocument doc) {
		return new Builder(DomType.ROOT, doc);
	}

	public DomStructureComparable(Object id, DomType type, String name, IDocument document, Position range, DomStructureComparable[] children) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.document = document;
		this.range = range;
		this.children = children;
		for (DomStructureComparable child : children) {
			child.parent = this;
		}
	}

	@Override
	public Object[] getChildren() {
		return children;
	}

	public DomType getDomType() {
		return type;
	}

	@Override
	public IDocument getDocument() {
		return document;
	}

	@Override
	public Position getRange() {
		return range;
	}

	public static Builder createElement(IDocument document, Builder parent, String name) {
		return new Builder(DomType.ELEMENT, document).parent(parent).name(name);		
	}
	
	public static Builder createText(IDocument document, Builder parent) {
		return new Builder(DomType.TEXT, document).parent(parent);		
	}
	
	/**
	 * 
	 * @param doc
	 * @param offset - opening tag '>' offset
	 * @return
	 * @throws BadLocationException
	 */
	private static int adjustStartPositionForElement(IDocument doc, int offset) throws BadLocationException {
		if (offset > 0) {
			DocumentRegion region = new DocumentRegion(doc, offset, offset);
			region = region.extendBeforeUntil('<');
			DocumentRegion before = region.textBeforeOnLine();
			if (before.isWhiteSpace()) {
				return before.getStart();
			} else {
				return region.getStart();
			}
			
		}
		return offset;
	}
	
	private static int adjustEndPositionForElement(IDocument doc, int offset) throws BadLocationException {
		DocumentRegion region = new DocumentRegion(doc, offset, offset);
		DocumentRegion after = region.textAfterOnLine();
		if (after.isWhiteSpace()) {
			DocumentRegion newLine = after.textAfter(2);
			char c1 = newLine.charAt(0);
			char c2 = newLine.charAt(1);
			if (c1 == '\n') {
				if (c2 == '\r') {
					return after.getEnd() + 2;
				} else {
					return after.getEnd() + 1;
				}
			}
		}
		return region.getEnd();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getType() {
		return this.type.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		DomStructureComparable other = (DomStructureComparable) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Includes opening and closing tags
	 * @return
	 */
	public String getTextContent() {
		try {
			return document.get(range.offset, range.length);
		} catch (BadLocationException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * Text between the tags
	 * @return
	 */
	public String getValue() {
		try {
			String s = getDocument().get(getRange().offset, getRange().length);
			int start = s.indexOf('>');
			int end = s.lastIndexOf('<');
			if (start >= 0 && start < end) {
				return s.substring(start + 1, end);
			} else {
				return "";
			}
		} catch (BadLocationException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	public DomStructureComparable getParent() {
		return parent;
	}

}
