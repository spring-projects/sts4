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

import static org.springframework.ide.eclipse.maven.pom.PomDocumentDiffer.differenceDirections;
import static org.springframework.ide.eclipse.maven.pom.PomDocumentDiffer.ignorePath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Difference;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Direction;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

@SuppressWarnings("restriction")
public class PomDocumentMerger extends DocumentMerger {
	
	private CompareConfiguration configuration;

	public PomDocumentMerger(IDocumentMergerInput input, CompareConfiguration configuration) {
		super(input);
		this.configuration = configuration;
	}
	
	private ArrayList<Diff> calculateDiffs() {
		ArrayList<Diff> diffs = new ArrayList<>();
		IDocument lDoc = getDocument(MergeViewerContentProvider.LEFT_CONTRIBUTOR);
		IDocument rDoc = getDocument(MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
		
		if (lDoc == null || rDoc == null) {
			return new ArrayList<>();
		}
		
		Position lRegion = getRegion(MergeViewerContentProvider.LEFT_CONTRIBUTOR);
		Position rRegion = getRegion(MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
		
		resetPositions(lDoc);
		resetPositions(rDoc);

		boolean ignoreWhiteSpace = true;
		
		
		List<Difference> diffNodes = PomDocumentDiffer.create(lDoc, rDoc)
				.filter(differenceDirections(configuration.isMirrored() ? Direction.LEFT : Direction.RIGHT)
						.and(ignorePath("project", "name"))
						.and(ignorePath("project", "description"))
						.and(ignorePath("project", "groupId"))
						.and(ignorePath("project", "version"))
						.and(ignorePath("project", "parent", "relativePath"))
				)
			.getDiffs();
		
		for (Difference d : diffNodes) {
				Diff difference = null;

				switch (d.direction) {
				case BOTH:
				case NONE:
				case LEFT:
				case RIGHT:
					difference = newDiff(
						null, d.direction == Direction.NONE ? RangeDifference.NOCHANGE : RangeDifference.CHANGE, null, null, 0, 0,
						lDoc, lRegion, d.leftRange.offset, d.leftRange.offset + d.leftRange.length,
						rDoc, rRegion, d.rightRange.offset, d.rightRange.offset + d.rightRange.length
					);
					diffs.add(difference);
					break;
				}
				
				if (difference != null) {
					// Extract the string for each contributor.
					String leftStr = "";
					String rightStr = "";
					try {
						leftStr= lDoc.get(d.leftRange.offset, d.leftRange.length);
						rightStr= rDoc.get(d.rightRange.offset, d.rightRange.length);
					} catch (BadLocationException e) {
						throw ExceptionUtil.unchecked(e);
					}

					// Indicate whether all contributors are whitespace
					if (ignoreWhiteSpace
							&& leftStr.trim().length() == 0
							&& rightStr.trim().length() == 0) {
						setDiffWhitespace(difference, true);
					}

					// If the diff is of interest, record it and generate the token diffs
					if (useChange(difference)) {
						recordChangeDiff(difference);
						if (leftStr.length() > 0 && rightStr.length() > 0) {
//							if (USE_MERGING_TOKEN_DIFF)
//								mergingTokenDiff(diff, aDoc, a, rDoc, d, lDoc, s);
//							else
								simpleTokenDiff(difference, null, null, rDoc, rightStr, lDoc, leftStr);
						}
					}
				}
			
		}
		return diffs;
	}
	
	
	
	@Override
	public void doDiff() throws CoreException {
		setChangeDiffs(new ArrayList<>());
		setAllDiffs(calculateDiffs());
	}

	private IDocument getDocument(char contributor) {
		try {
			Method method = DocumentMerger.class.getDeclaredMethod("getDocument", char.class);
			method.setAccessible(true);
			return (IDocument) method.invoke(this, contributor);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private Position getRegion(char contributor) {
		try {
			Method method = DocumentMerger.class.getDeclaredMethod("getRegion", char.class);
			method.setAccessible(true);
			return (Position) method.invoke(this, contributor);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void resetPositions(IDocument doc) {
		try {
			Method method = DocumentMerger.class.getDeclaredMethod("resetPositions", IDocument.class);
			method.setAccessible(true);
			method.invoke(this, doc);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void recordChangeDiff(Diff diff) {
		try {
			Method method = DocumentMerger.class.getDeclaredMethod("recordChangeDiff", Diff.class);
			method.setAccessible(true);
			method.invoke(this, diff);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void simpleTokenDiff(final Diff baseDiff,
			IDocument ancestorDoc, String a,
			IDocument rightDoc, String d,
			IDocument leftDoc, String s) {
		try {
			Method method = DocumentMerger.class.getDeclaredMethod("simpleTokenDiff", Diff.class,
					IDocument.class, String.class,
					IDocument.class, String.class,
					IDocument.class, String.class);
			method.setAccessible(true);
			method.invoke(this, baseDiff, ancestorDoc, a, rightDoc, d, leftDoc, s);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void setChangeDiffs(ArrayList<Diff> l) {
		try {
			Field field = DocumentMerger.class.getDeclaredField("fChangeDiffs");
			field.setAccessible(true);
			field.set(this, l);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void setAllDiffs(ArrayList<Diff> l) {
		try {
			Field field = DocumentMerger.class.getDeclaredField("fAllDiffs");
			field.setAccessible(true);
			field.set(this, l);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private Diff newDiff(Diff parent, int dir, IDocument ancestorDoc, Position aRange, int ancestorStart,
				int ancestorEnd,
							 IDocument leftDoc, Position lRange, int leftStart, int leftEnd,
							 IDocument rightDoc, Position rRange, int rightStart, int rightEnd) {
								 
		try {
			Constructor<Diff> constructor = Diff.class.getDeclaredConstructor(DocumentMerger.class, Diff.class, int.class,
					IDocument.class, Position.class, int.class, int.class,
					IDocument.class, Position.class, int.class, int.class,
					IDocument.class, Position.class, int.class, int.class);
			constructor.setAccessible(true);
			return (Diff) constructor.newInstance(this, parent, dir, ancestorDoc, aRange, ancestorStart, ancestorEnd,
					leftDoc, lRange, leftStart, leftEnd, rightDoc, rRange, rightStart, rightEnd);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
	
	private void setDiffWhitespace(Diff diff, boolean whitespace) {
		try {
			Field field = Diff.class.getDeclaredField("fIsWhitespace");
			field.setAccessible(true);
			field.set(diff, whitespace);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

}
