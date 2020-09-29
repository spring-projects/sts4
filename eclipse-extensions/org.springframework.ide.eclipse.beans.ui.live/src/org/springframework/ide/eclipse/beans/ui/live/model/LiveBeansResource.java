/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansResource extends LiveBeansGroup<LiveBean> {

	private final TypeLookup lookupSession;

	public LiveBeansResource(String label, TypeLookup lookupSession) {
		super(label);
		attributes.put(LiveBean.ATTR_RESOURCE, label);
		this.lookupSession = lookupSession;
	}
	
	public TypeLookup getTypeLookup() {
		return this.lookupSession;
	}

	@Override
	public String getDisplayName() {
		// compute the display name the first time it's needed
		String label = getLabel();
		if (label.equalsIgnoreCase("null")) {
			return "Container Generated";
		} else {
			// Expecting the label to contain some form of
			// "[file/path/to/resource.ext]" so we're going to parse out the
			// last segment of the file path.
			int indexStart = label.indexOf("[");
			int indexEnd = label.lastIndexOf("]");
			if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
				return label.substring(indexStart + 1, indexEnd);
			} else {
				return label;
			}
		}
	}

	public String getFileName() {
		String label = getLabel();
		int indexStart = label.lastIndexOf("/");
		int indexEnd = label.lastIndexOf("]");
		if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
			return label.substring(indexStart + 1, indexEnd);
		}
		return null;
	}

	public String getFileExtension() {
		String filename = getFileName();
		if (filename != null) {
			int idx = filename.lastIndexOf('.');
			if (idx >= 0 && idx < filename.length() - 1) {
				return filename.substring(idx + 1);
			}
		}
		return null;
	}

}
