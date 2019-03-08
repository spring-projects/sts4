/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UriUtilTest {
	
	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void normalize_deleted_folder_uri() throws Exception {
		File folder = temp.newFolder();
		assertTrue(folder.exists());
		
		String folderUri = folder.toURI().toString();
		assertTrue(folderUri.endsWith("/"));
		folderUri = UriUtil.normalize(folderUri);

		folder.delete();
		assertFalse(folder.exists());
		String deletedFolderUri = folder.toURI().toString();
		assertFalse(deletedFolderUri.endsWith("/"));
		deletedFolderUri = UriUtil.normalize(deletedFolderUri);
		
		assertEquals(folderUri, deletedFolderUri);
	}

	@Test
	public void normalize_slashes() throws Exception {
		String[] cases = {
				"file:/foo/bar",
				"file:/foo/bar/",
				"file:///foo/bar/",
				"file:///foo/bar",
		};
		String previous = null;
		for (String uri : cases) {
			String normalized = UriUtil.normalize(uri); 
			if (previous!=null) {
				assertEquals(previous, normalized);
			}
			previous = normalized;
		}
	}

	@Test
	public void contains() throws Exception {
		String[] roots = {
				"file:/foo/bar",
				"file:/foo/bar/",
				"file:///foo/bar/",
				"file:///foo/bar",
		};
		String[] children = {
				"file:/foo/bar/child",
				"file:/foo/bar/child/baby",
				"file:///foo/bar/foo.txt",
				"file:///foo/bar/",
				"file:///foo/bar"
		};
		for (String root : roots) {
			for (String child : children) {
				//Note: implementation of 'contains' assumes normalized input so...
				root = UriUtil.normalize(root);
				child = UriUtil.normalize(child);
				
				assertTrue(UriUtil.contains(root, child));
			}
		}
		
		String[] not_children = {
				"file:/foo",
				"file:/foo/",
				"file:///foo",
				"file:///foo/",
				"file:/foo/whatever",
				"file:/foo/whatever/",
				"file:///foo/whatever",
				"file:///foo/whatever/",
				"file:///foo/barack",	//starts with '/foo/bar' but is not a child! 
				"file:///foo/barack/",	//starts with '/foo/bar' but is not a child! 
				"file:/foo/barack",		//starts with '/foo/bar' but is not a child! 
				"file:/foo/barack/"		//starts with '/foo/bar' but is not a child! 
		};
		for (String root : roots) {
			for (String child : not_children) {
				//Note: implementation of 'contains' assumes normalized input so...
				root = UriUtil.normalize(root);
				child = UriUtil.normalize(child);
				
				assertFalse(UriUtil.contains(root, child));
			}
		}
	}
	
}
