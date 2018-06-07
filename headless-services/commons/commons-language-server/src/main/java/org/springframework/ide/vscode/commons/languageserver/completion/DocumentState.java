/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.Direction;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.OffsetTransformer;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * DocumentState provides methods to modify a document, its methods accept
 * offsets expressed relative to the original document contents and keeps track
 * of a OffsetTransformer that maps them to offsets in the current document.
 */
public class DocumentState implements IDocumentState {
	IDocument doc; //may be null, in which case no actual modifications are performed
	OffsetTransformer org2new = DocumentEdits.NULL_TRANSFORM;
	int selection = -1; //-1 Means no edits where applied that change selection so
								// the current selection is unknown

	public DocumentState(IDocument doc) {
		this.doc = doc;
	}

	public OffsetTransformer getOrg2New() {
		return this.org2new;
	}

	public int getCursor() {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.springframework.ide.vscode.commons.languageserver.completion.IDocumentState#insert(boolean, int, java.lang.String)
	 */
	@Override
	public void insert(boolean grabCursor, int start, final String text) throws BadLocationException {
		final int tStart = org2new.transform(start, Direction.AFTER);
		if (!text.isEmpty()) {
			if (doc!=null) {
				doc.replace(tStart, 0, text);
			}
			final OffsetTransformer parent = org2new;
			org2new = new OffsetTransformer() {
				@Override
				public int transform(int org, Direction dir) {
					int tOffset = parent.transform(org, dir);
					if (tOffset<tStart) {
						return tOffset;
					} else if (tOffset>tStart) {
						return tOffset + text.length();
					} else /* tOffset==tStart*/ {
						if (dir==Direction.BEFORE) {
							return tOffset;
						} else {
							return tOffset + text.length();
						}
					}
				}
			};
		}
		if (grabCursor) {
			selection = tStart+text.length();
		} else if (selection > tStart) {
			selection += text.length();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.ide.vscode.commons.languageserver.completion.IDocumentState#delete(boolean, int, int)
	 */
	@Override
	public void delete(boolean grabCursor, final int start, final int end) throws BadLocationException {
		final int tStart = org2new.transform(start, Direction.AFTER);
		if (end>start) { // skip work for 'delete nothing' op
			final int tEnd = org2new.transform(end, Direction.BEFORE);
			if (tEnd>tStart) { // skip work for 'delete nothing' op
				if (doc!=null) {
					doc.replace(tStart, tEnd-tStart, "");
				}

				final OffsetTransformer parent = org2new;
				org2new = new OffsetTransformer() {
					@Override
					public int transform(int org, Direction dir) {
						int tOffset = parent.transform(org, dir);
						if (tOffset<=tStart) {
							return tOffset;
						} else if (tOffset>=tEnd) {
							return tOffset - tEnd + tStart;
						} else {
							return start;
						}
					}
				};
			}
		}
		if (grabCursor) {
			selection = tStart;
		} else if (selection>tStart) {
			int len = end - start;
			if (len > 0) {
				selection = Math.max(tStart, selection-len);
			}
		}
	}

	@Override
	public String toString() {
		if (doc==null) {
			return super.toString();
		}
		StringBuilder buf = new StringBuilder();
		buf.append("DocumentState(\n");
		buf.append(doc.get()+"\n");
		buf.append(")\n");
		return buf.toString();
	}

	public IDocument getDocument() {
		return this.doc;
	}
}