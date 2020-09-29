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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.springsource.ide.eclipse.commons.frameworks.core.util.DocumentRegion;

public class XmlDocumentDiffer {

	private IDocument lDoc;
	private IDocument rDoc;
	private IdProviderRegistry idProviders;
	private Predicate<Difference> filter = x -> true;

	public XmlDocumentDiffer(IDocument document1, IDocument document2) {
		this.idProviders = new IdProviderRegistry();
		this.lDoc = document1;
		this.rDoc = document2;
	}

	public List<Difference> getDiffs() {
		XMLStructureCreator structureCreator = new XMLStructureCreator(idProviders);
		DomStructureComparable leftStructure = structureCreator.createStructure(lDoc);
		
		DomStructureComparable rightStructure = structureCreator.createStructure(rDoc);
		
		if (lDoc == null || rDoc == null)
			return Collections.emptyList();
		
		if (leftStructure == null && rightStructure == null) {
			return Collections.emptyList();
		}
		
		if (leftStructure == null && rightStructure != null) {
			List<Difference> diffs = new ArrayList<>();
			diffs.add(new Difference(Direction.LEFT, new Position(0, 0), new Position(0, rDoc.getLength()), leftStructure, rightStructure));
			return diffs;
		}

		if (leftStructure != null && rightStructure == null) {
			List<Difference> diffs = new ArrayList<>();
			diffs.add(new Difference(Direction.RIGHT, new Position(0, lDoc.getLength()), new Position(0, 0), leftStructure, rightStructure));
			return diffs;
		}
		
		Differencer differencer = new Differencer() {
			@Override
			protected boolean contentsEqual(Object o1,
					char contributor1, Object o2, char contributor2) {
				boolean ignoreWhiteSpace = true;
				String s1, s2;
				if (hasChildren(o1) && hasChildren(o2)) {
					return true;
				}
				s1 = structureCreator.getContents(o1, ignoreWhiteSpace);
				s2 = structureCreator.getContents(o2, ignoreWhiteSpace);
				if (s1 == null || s2 == null)
					return false;
				return s1.equals(s2);
			}
			private boolean hasChildren(Object o) {
				if (o instanceof DomStructureComparable) {
					return ((DomStructureComparable)o).getChildren().length > 0;
				}
				return false;
			}
		};
		DiffNode differences = (DiffNode) differencer.findDifferences(false, new NullProgressMonitor(), null, null, leftStructure, rightStructure);
		List<IDiffElement> leafs = new ArrayList<>();
		gatherLeafs(differences, leafs);
	
		List<Difference> allDiffs = new ArrayList<>();
		
		if (leafs.isEmpty()) {
			return Collections.emptyList();
		}
		
		for (IDiffElement e : leafs) {
			DiffNode diffNode = (DiffNode) e;
			DomStructureComparable left = (DomStructureComparable) diffNode.getLeft();
			DomStructureComparable right = (DomStructureComparable) diffNode.getRight();
			Difference difference = null;
			DiffNode parentNode;
			Position insertLocation;
			switch (diffNode.getKind() & Differencer.CHANGE_TYPE_MASK) {
			case Differencer.CHANGE:
				difference = new Difference(Direction.BOTH, left.getRange(), right.getRange(), left, right);
				break;
			case Differencer.ADDITION:
				parentNode = (DiffNode) diffNode.getParent();
				insertLocation = parentNode == null ? new Position(lDoc.getLength(), 0) : insertPosition((DomStructureComparable)parentNode.getLeft());
				difference = new Difference(Direction.LEFT, insertLocation, right.getRange(), left, right);
				break;
			case Differencer.DELETION:
				parentNode = (DiffNode) diffNode.getParent();
				insertLocation = parentNode == null ? new Position(rDoc.getLength(), 0) : insertPosition((DomStructureComparable)parentNode.getRight());
				difference = new Difference(Direction.RIGHT, left.getRange(), insertLocation, left, right);
				break;
			}
			if (this.filter.test(difference)) {
				allDiffs.add(difference);
			}
		}
		
		List<Position> leftChanges = new ArrayList<>();
		List<Position> rightChanges = new ArrayList<>();
		
		for (Difference d : allDiffs) {
			leftChanges.add(d.leftRange);
			rightChanges.add(d.rightRange);
		}
		
		Comparator<Position> positionComparator = new Comparator<Position>() {

			@Override
			public int compare(Position o1, Position o2) {
				return o1.offset - o2.offset;
			}
			
		};
		
		Collections.sort(leftChanges, positionComparator);
		Collections.sort(rightChanges, positionComparator);
		
		int prevLeftEnd = 0;
		int prevRightEnd = 0;
		for (int i = 0; i < leftChanges.size(); i++) {
			Position leftChange = leftChanges.get(i);
			Position rightChange = rightChanges.get(i);
			allDiffs.add(new Difference(Direction.NONE, new Position(prevLeftEnd, leftChange.offset - prevLeftEnd),
					new Position(prevRightEnd, rightChange.offset - prevRightEnd), null, null));
			prevLeftEnd = leftChange.offset + leftChange.length;
			prevRightEnd = rightChange.offset + rightChange.length;
		}
		if (prevLeftEnd < lDoc.getLength() || prevRightEnd < rDoc.getLength()) {
			allDiffs.add(new Difference(
				Direction.NONE,
				new Position(prevLeftEnd, lDoc.getLength() - prevLeftEnd),
				new Position(prevRightEnd, rDoc.getLength() - prevRightEnd),
				null,
				null
			));
		}
		
		Collections.sort(allDiffs, new Comparator<Difference>() {

			@Override
			public int compare(Difference o1, Difference o2) {
				int compareLeft = positionComparator.compare(o1.leftRange, o2.leftRange);
				if (compareLeft == 0) {
					return positionComparator.compare(o1.rightRange, o2.rightRange);
				} else {
					return compareLeft;
				}
			}
			
		});
		
		return allDiffs;
	}
	
	private Position insertPosition(DomStructureComparable parent) {
		int end = parent.getRange().offset + parent.getRange().length - 1;
		DocumentRegion region = new DocumentRegion(parent.getDocument(), end, end);
		region = region.extendBeforeUntil('<');
		DocumentRegion before = region.textBeforeOnLine();
		if (before.isWhiteSpace()) {
			return new Position(before.getStart(), 0);
		} else {
			return new Position(region.getStart(), 0);
		}
	}
	
	private void gatherLeafs(IDiffElement node, List<IDiffElement> leafs) {
		if (node instanceof IDiffContainer) {
			IDiffContainer container = (IDiffContainer) node;
			if (container.hasChildren()) {
				for (IDiffElement child : container.getChildren()) {
					gatherLeafs(child, leafs);
				}
			} else {
				leafs.add(node);
			}
		} else if (node != null){
			leafs.add(node);
		}
	}
	
	public static class Difference {
		final public Direction direction;
		final public Position leftRange;
		final public Position rightRange;
		final public DomStructureComparable leftComparable;
		final public DomStructureComparable rightComparable;
		public Difference(Direction direction, Position leftRange, Position rightRange, DomStructureComparable left, DomStructureComparable right) {
			this.direction = direction;
			this.leftRange = leftRange;
			this.rightRange = rightRange;
			this.leftComparable = left;
			this.rightComparable = right;
		} 
	}
	
	public static enum Direction {
		NONE(Differencer.NO_CHANGE),
		LEFT(Differencer.ADDITION), // inserted on the left
		RIGHT(Differencer.DELETION), // inserted on the right
		BOTH(Differencer.CHANGE);

		final int differencerKind;
		
		private Direction(int eclipseKind) {
			this.differencerKind = eclipseKind;
		}

		int getDifferencerKind() {
			return differencerKind;
		}
	}

	public XmlDocumentDiffer idProvider(Predicate<DomStructureComparable.Builder> selector, IdProvider provider) {
		this.idProviders.add(selector, provider);
		return this;
	}

	public XmlDocumentDiffer filter(Predicate<Difference> filter) {
		this.filter = filter;
		return this;
	}

}
