/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.internal.core.commandhistory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.springsource.ide.eclipse.commons.core.Entry;
import org.springsource.ide.eclipse.commons.core.ICommandHistory;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;


/**
 * This provides a light-weight command history that is persisted in the user's
 * workspace.
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public class CommandHistory implements Iterable<Entry>, ICommandHistory {

	/**
	 * Our ISaveParticipant, responsible for persisting the CommandHistory in
	 * the workspace.
	 */
	private class CommandHistorySaver implements ISaveParticipant {

		public void doneSaving(ISaveContext context) {
		}

		public void prepareToSave(ISaveContext context) throws CoreException {
		}

		public void rollback(ISaveContext context) {
		}

		public void saving(ISaveContext context) throws CoreException {
			if (isDirty()) {
				try {
					save(getSavePath());
				}
				catch (Exception e) {
					throw new CoreException(CorePlugin.createErrorStatus("Couldn't save command history", e));
				}
				context.needSaveNumber();
			}
		}

	}

	private static final String HISTORY_FILE_NAME = ".commandhistory";

	/**
	 * If the number of history entries exceeds maxSize, older items will be
	 * automatically dropped.
	 */
	private int maxSize = DEFAULT_MAX_SIZE;

	private boolean isDirty;

	private LinkedList<Entry> history = new LinkedList<Entry>();

	private final String natureId;

	private final String historyId;

	public CommandHistory(String historyId, String natureId) {
		this.natureId = natureId;
		this.historyId = historyId;
	}

	/**
	 * Create an "auto saving" CommandHistory and register it as a save
	 * participant with the workspace.
	 */
	public CommandHistory(String historyId, String natureId, boolean persist) throws CoreException {
		this.natureId = natureId;
		this.historyId = historyId;
		if (persist) {
			CommandHistorySaver saver = new CommandHistorySaver();
			// Method addSaveParticipant(String, ISaveParticipant) is deprecated
			// on 3.6, but the alternative does not exist in 3.5
			ResourcesPlugin.getWorkspace().addSaveParticipant(CorePlugin.getDefault(), saver);
			load(getSavePath());
		}
	}

	/**
	 * Add an element to the history. If a similar element already exists in the
	 * history, the older element is removed before adding the new one.
	 * <p>
	 * Adding an element beyond the maxSize will result in the oldest item being
	 * discarded.
	 */
	public void add(Entry entry) {
		history.remove(entry);
		history.addFirst(entry);
		discardOldEntries();
		isDirty = true;
	}

	public void clear() {
		history.clear();
		isDirty = true;
	}

	/**
	 * Throw away old entries until our size obeys the maxSize constraint.
	 */
	private void discardOldEntries() {
		while (size() > getMaxSize()) {
			history.removeLast();
		}
	}

	/**
	 * Return the last added element.
	 */
	public Entry getLast() {
		// elements in backing collection are actually in inverse order
		return history.getFirst();
	}

	/**
	 * @return The number of items in the history is limited to.
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Retrieve a List of most recent entries, (most recent first).
	 * @param limit Return at most this many elements.
	 */
	public List<Entry> getRecentValid(int limit) {
		ArrayList<Entry> result = new ArrayList<Entry>(limit);
		for (Entry entry : validEntries()) {
			if (limit-- <= 0) {
				return result;
			}
			result.add(entry);
		}
		return result;
	}

	private IPath getSavePath() {
		return CorePlugin.getDefault().getStateLocation().append(historyId + HISTORY_FILE_NAME);
	}

	/**
	 * Did this history get changed since it was last saved?
	 */
	public boolean isDirty() {
		return isDirty;
	}

	public boolean isEmpty() {
		return history.isEmpty();
	}

	/**
	 * Iterator that starts from the most recently added element.
	 */
	public Iterator<Entry> iterator() {
		return history.iterator();
	}

	/**
	 * Load history contents from a file. If the file does not exist we silently
	 * assume the history should be empty.
	 * <p>
	 * If there are errors loading file we keep the current history and log an
	 * exception.
	 */
	public void load(File file) {
		try {
			if (file.exists()) {
				FileInputStream fIn = new FileInputStream(file);
				ObjectInputStream oIn = null;
				try {
					oIn = new ObjectInputStream(fIn);
					load(oIn);
					isDirty = false;
				}
				finally {
					if (oIn != null) {
						oIn.close();
					}
					else if (fIn != null) {
						fIn.close();
					}
				}
			}
		}
		catch (Exception e) {
			CorePlugin.log("Could not restore the command history", e);
			if (file.exists()) {
				// File is corrupt...
				file.delete();
			}
		}
	}

	private void load(IPath savePath) {
		load(savePath.toFile());
	}

	/**
	 * Load the state from an InputOutputStream, let someone else worry where
	 * this stream came from and how to handle error / exception making sure the
	 * stream is closed no matter what.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int newMaxSize = in.readInt();
		Entry[] elements = new Entry[in.readInt()];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = (Entry) in.readObject();
		}

		// We could do this in one go, but it is slightly nicer not to modify
		// the state of this history object until we are successful reading
		// all the elements.

		maxSize = newMaxSize;
		history = new LinkedList<Entry>();
		for (Entry element : elements) {
			history.add(element);
		}
		isDirty = false;
	}

	/**
	 * Save this history to a file
	 * @throws IOException
	 */
	public void save(File file) throws IOException {
		if (isDirty()) {
			FileOutputStream fOut = new FileOutputStream(file);
			ObjectOutputStream oOut = null;
			try {
				oOut = new ObjectOutputStream(fOut);
				save(oOut);
				isDirty = false;
			}
			finally {
				if (oOut != null) {
					oOut.close();
				}
				else if (fOut != null) {
					fOut.close();
				}
			}
		}
	}

	/**
	 * Save this history to a file
	 * @throws IOException
	 */
	private void save(IPath file) throws IOException {
		save(file.toFile());
	}

	/**
	 * Save the state to an ObjectOutputStream, let someone else worry where
	 * this stream came from and how to handle error / exception making sure the
	 * stream is closed no matter what.
	 */
	private void save(ObjectOutputStream out) throws IOException {
		out.writeInt(maxSize);
		out.writeInt(size());
		for (Entry entry : history) {
			out.writeObject(entry);
		}
	}

	/**
	 * Set the maxSize value. This will limit the number of items that will be
	 * retained in the history. When the limit is exceeded, older items are
	 * discarded.
	 */
	public void setMaxSize(int max) {
		Assert.isLegal(max > 0);
		this.maxSize = max;
		discardOldEntries();
		isDirty = true;
	}

	public int size() {
		return history.size();
	}

	/**
	 * Returns an array of the elements in the history, with the newest item at
	 * position 0.
	 */
	public Entry[] toArray() {
		Entry[] result = new Entry[size()];
		int i = 0;
		for (Entry entry : this) {
			result[i++] = entry;
		}
		return result;
	}

	/**
	 * @return an Iterable (suitable for "foreach" iteration) providing only
	 * Entry's who's projects are valid open projects in the workspace.
	 */
	public Iterable<Entry> validEntries() {
		return new ValidProjectFilter(this, natureId);
	}

}
