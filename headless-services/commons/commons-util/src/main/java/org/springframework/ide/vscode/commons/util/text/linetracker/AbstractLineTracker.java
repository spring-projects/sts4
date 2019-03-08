/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util.text.linetracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.IRegion;

/**
 * Abstract implementation of <code>ILineTracker</code>. It lets the definition of line
 * delimiters to subclasses. Assuming that '\n' is the only line delimiter, this abstract
 * implementation defines the following line scheme:
 * <ul>
 * <li> "" -> [0,0]
 * <li> "a" -> [0,1]
 * <li> "\n" -> [0,1], [1,0]
 * <li> "a\n" -> [0,2], [2,0]
 * <li> "a\nb" -> [0,2], [2,1]
 * <li> "a\nbc\n" -> [0,2], [2,3], [5,0]
 * </ul>
 * <p>
 * This class must be subclassed.
 * </p>
 */
public abstract class AbstractLineTracker implements ILineTracker, ILineTrackerExtension {

	/**
	 * Tells whether this class is in debug mode.
	 *
	 * @since 3.1
	 */
	private static final boolean DEBUG= false;

	/**
	 * Combines the information of the occurrence of a line delimiter. <code>delimiterIndex</code>
	 * is the index where a line delimiter starts, whereas <code>delimiterLength</code>,
	 * indicates the length of the delimiter.
	 */
	protected static class DelimiterInfo {
		public int delimiterIndex;
		public int delimiterLength;
		public String delimiter;
	}

	/**
	 * Representation of replace and set requests.
	 *
	 * @since 3.1
	 */
	protected static class Request {
		public final int offset;
		public final int length;
		public final String text;

		public Request(int offset, int length, String text) {
			this.offset= offset;
			this.length= length;
			this.text= text;
		}

		public Request(String text) {
			this.offset= -1;
			this.length= -1;
			this.text= text;
		}

		public boolean isReplaceRequest() {
			return this.offset > -1 && this.length > -1;
		}
	}

	/**
	 * The active rewrite session.
	 *
	 * @since 3.1
	 */
	private DocumentRewriteSession fActiveRewriteSession;
	
	/**
	 * The list of pending requests.
	 *
	 * @since 3.1
	 */
	private List<Request> fPendingRequests;
	/**
	 * The implementation that this tracker delegates to.
	 *
	 * @since 3.2
	 */
	private ILineTracker fDelegate= new ListLineTracker() {
		@Override
		public String[] getLegalLineDelimiters() {
			return AbstractLineTracker.this.getLegalLineDelimiters();
		}

		@Override
		protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
			return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
		}
	};
	/**
	 * Whether the delegate needs conversion when the line structure is modified.
	 */
	private boolean fNeedsConversion= true;

	/**
	 * Creates a new line tracker.
	 */
	protected AbstractLineTracker() {
	}

	@Override
	public int computeNumberOfLines(String text) {
		return fDelegate.computeNumberOfLines(text);
	}

	@Override
	public String getLineDelimiter(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineDelimiter(line);
	}

	@Override
	public IRegion getLineInformation(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineInformation(line);
	}

	@Override
	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineInformationOfOffset(offset);
	}

	@Override
	public int getLineLength(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineLength(line);
	}

	@Override
	public int getLineNumberOfOffset(int offset) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineNumberOfOffset(offset);
	}

	@Override
	public int getLineOffset(int line) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getLineOffset(line);
	}

	@Override
	public int getNumberOfLines() {
		try {
			checkRewriteSession();
		} catch (BadLocationException x) {
			// TODO there is currently no way to communicate that exception back to the document
		}
		return fDelegate.getNumberOfLines();
	}

	@Override
	public int getNumberOfLines(int offset, int length) throws BadLocationException {
		checkRewriteSession();
		return fDelegate.getNumberOfLines(offset, length);
	}

	@Override
	public void set(String text) {
		if (hasActiveRewriteSession()) {
			fPendingRequests.clear();
			fPendingRequests.add(new Request(text));
			return;
		}

		fDelegate.set(text);
	}

	@Override
	public void replace(int offset, int length, String text) throws BadLocationException {
		if (hasActiveRewriteSession()) {
			fPendingRequests.add(new Request(offset, length, text));
			return;
		}

		checkImplementation();

		fDelegate.replace(offset, length, text);
	}

	/**
	 * Converts the implementation to be a {@link TreeLineTracker} if it isn't yet.
	 *
	 * @since 3.2
	 */
	private void checkImplementation() {
		if (fNeedsConversion) {
			fNeedsConversion= false;
			fDelegate= new TreeLineTracker((ListLineTracker) fDelegate) {
				@Override
				protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
					return AbstractLineTracker.this.nextDelimiterInfo(text, offset);
				}

				@Override
				public String[] getLegalLineDelimiters() {
					return AbstractLineTracker.this.getLegalLineDelimiters();
				}
			};
		}
	}

	/**
	 * Returns the information about the first delimiter found in the given text starting at the
	 * given offset.
	 *
	 * @param text the text to be searched
	 * @param offset the offset in the given text
	 * @return the information of the first found delimiter or <code>null</code>
	 */
	protected abstract DelimiterInfo nextDelimiterInfo(String text, int offset);

	@Override
	public final void startRewriteSession(DocumentRewriteSession session) {
		if (fActiveRewriteSession != null)
			throw new IllegalStateException();
		fActiveRewriteSession= session;
		fPendingRequests= new ArrayList<>(20);
	}

	@Override
	public final void stopRewriteSession(DocumentRewriteSession session, String text) {
		if (fActiveRewriteSession == session) {
			fActiveRewriteSession= null;
			fPendingRequests= null;
			set(text);
		}
	}

	/**
	 * Tells whether there's an active rewrite session.
	 *
	 * @return <code>true</code> if there is an active rewrite session, <code>false</code>
	 *         otherwise
	 * @since 3.1
	 */
	protected final boolean hasActiveRewriteSession() {
		return fActiveRewriteSession != null;
	}

	/**
	 * Flushes the active rewrite session.
	 *
	 * @throws BadLocationException in case the recorded requests cannot be processed correctly
	 * @since 3.1
	 */
	protected final void flushRewriteSession() throws BadLocationException {
		if (DEBUG)
			System.out.println("AbstractLineTracker: Flushing rewrite session: " + fActiveRewriteSession); //$NON-NLS-1$

		Iterator<Request> e= fPendingRequests.iterator();

		fPendingRequests= null;
		fActiveRewriteSession= null;

		while (e.hasNext()) {
			Request request= e.next();
			if (request.isReplaceRequest())
				replace(request.offset, request.length, request.text);
			else
				set(request.text);
		}
	}

	/**
	 * Checks the presence of a rewrite session and flushes it.
	 *
	 * @throws BadLocationException in case flushing does not succeed
	 * @since 3.1
	 */
	protected final void checkRewriteSession() throws BadLocationException {
		if (hasActiveRewriteSession())
			flushRewriteSession();
	}
}
