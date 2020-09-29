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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyFileStore;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class PropertyFileStoreTest {

	static Random rnd = new Random();

	File file;

	@Before
	public void setup() {
		file = new File("test-"+rnd.nextInt(10000000)+".properties");
	}

	@After
	public void tearDown() {
		FileUtils.deleteQuietly(file);
	}

	@Test
	public void basicStoreAndReload() throws Exception {
		{
			PropertyFileStore store = new PropertyFileStore(file);
			assertTrue(store.isEmpty());

			store.put("test", "something");
			store.put("foo", "bar");
			assertFalse(store.isEmpty());

			assertProperties(store,
					"test", "something",
					"foo", "bar"
			);

			store.sync();
			assertPermissions(file);
		}

		{
			PropertyFileStore store = new PropertyFileStore(file);

			assertProperties(store,
					"test", "something",
					"foo", "bar"
			);
		}
	}

	@Test
	public void putNullValues() throws Exception {
		//Putting null values is supposed to be equivalent to removing a property.
		{
			PropertyFileStore store = new PropertyFileStore(file);
			assertTrue(store.isEmpty());

			store.put("test", "something");
			store.put("foo", "bar");
			assertFalse(store.isEmpty());

			assertProperties(store,
					"test", "something",
					"foo", "bar"
			);

			store.put("test", null);

			assertProperties(store,
					"foo", "bar"
			);

			store.sync(); //Before reloading it we must be sure all is saved.
						// Store saving is automatic, but not synchronous.
		}

		{
			PropertyFileStore store = new PropertyFileStore(file);

			assertProperties(store,
					"foo", "bar"
			);
		}
	}

	private void assertProperties(PropertyFileStore store, String... propsAndValues) {
		assertTrue("Numer of props and value must be even (alternating prop and value)",propsAndValues.length%2 == 0);
		Builder<String, String> builder = ImmutableMap.builder();
		String p=null, v;
		for (int i = 0; i < propsAndValues.length; i++) {
			if (i%2==0) {
				p = propsAndValues[i];
			} else {
				v = propsAndValues[i];
				builder.put(p, v);
			}
		}
		ImmutableMap<String, String> expected = builder.build();
		assertEquals(expected, store.asMap());
	}

	private static void assertPermissions(File file) throws IOException {
		if (OsUtils.isWindows()) {
			//skip windows for now
		} else {
			Path path = file.toPath();
			Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
			assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE), perms);
		}
	}
}
