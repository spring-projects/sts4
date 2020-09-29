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
package org.springsource.ide.eclipse.commons.core.pstore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Implementation of IPropertyStore backed by a .properties file.
 * The properties file is 'private'. I.e. it is created such that only
 * the onwer of the file is allowed access to it.
 */
public class PropertyFileStore implements IPropertyStore {

	final private File file;
	final private Properties props = new Properties();

	Job flushProperties = new Job("Save private properties") {

		boolean errorLogged = false;

		{
			setSystem(true);
		}
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				createFileIfNeeded(file);
				try (OutputStream out = new FileOutputStream(file)) {
					props.store(out, "Private properties");
				}
			} catch (IOException e) {
				if (!errorLogged) {
					errorLogged = true; //complaining once about failure to store props is enough!
					Log.log(e);
				}
			}
			return Status.OK_STATUS;
		}
	};

	protected void createFileIfNeeded(File f) throws IOException {
		if (!f.exists()) {
			createFile(f);
			setPermissions(f);
		}
	}

	protected void setPermissions(File f) throws IOException {
		if (OsUtils.isWindows()) {
			setWindowsPerissions(f);
		} else {
			setUnixPermissions(f);
		}
	}

	protected void setWindowsPerissions(File f) throws IOException {
		Path file = f.toPath();
		AclFileAttributeView aclAttr = Files.getFileAttributeView(file, AclFileAttributeView.class);

		UserPrincipalLookupService upls = file.getFileSystem().getUserPrincipalLookupService();
		UserPrincipal user = upls.lookupPrincipalByName(System.getProperty("user.name"));
		AclEntry.Builder builder = AclEntry.newBuilder();
		builder.setPermissions( EnumSet.allOf(AclEntryPermission.class));
// This was here before but it seems not enough to actually allow reading the file:
//		builder.setPermissions( EnumSet.of(
//				AclEntryPermission.READ_DATA,
//				AclEntryPermission.WRITE_DATA,
//				AclEntryPermission.APPEND_DATA,
//				AclEntryPermission.READ_ACL,
//				AclEntryPermission.WRITE_ACL,
//				AclEntryPermission.READ_ATTRIBUTES,
//				AclEntryPermission.WRITE_ATTRIBUTES,
//				AclEntryPermission.READ_NAMED_ATTRS,
//				AclEntryPermission.WRITE_NAMED_ATTRS,
//				AclEntryPermission.DELETE
//		));
		builder.setPrincipal(user);
		builder.setType(AclEntryType.ALLOW);
		aclAttr.setAcl(Collections.singletonList(builder.build()));
	}

	protected void setUnixPermissions(File f) throws IOException {
		Files.setPosixFilePermissions(file.toPath(), EnumSet.of(
				PosixFilePermission.OWNER_READ,
				PosixFilePermission.OWNER_WRITE)
		);
	}

	protected void createFile(File f) throws IOException {
		if (f.getParentFile() != null) {
			f.getParentFile().mkdirs();
		}
		f.createNewFile();
	}

	public PropertyFileStore(File file) {
		this.file = file;
		try {
			if (file.exists()) {
				try (InputStream in = new FileInputStream(file)) {
					props.load(in);
				}
			}
		} catch (IOException e) {
			//Log and move on. This is not 'critical'. Maybe the file is corrupt and will be overwritten
			// or something else is wrong and we won't be able to persist props. But at least
			//props will be working in memory.
			Log.log(e);
		}
	}

	@Override
	public synchronized String get(String key) {
		return props.getProperty(key);
	}

	@Override
	public void put(String key, String value) throws Exception {
		Object oldValue = nullPut(key, value);
		if (!Objects.equals(oldValue, value)) {
			flushProperties.schedule();
		}
	}

	private Object nullPut(String key, String value) {
		if (value==null) {
			return props.remove(key);
		} else {
			return props.put(key, value);
		}
	}

	public boolean isEmpty() {
		return props.isEmpty();
	}

	/**
	 * Blocks the current thread until all dirty data has been written to disk. This meant for testing purposes.
	 * E.g. a test that wants to check contents of the backing file after performing ops on the store.
	 */
	public void sync() throws InterruptedException {
		flushProperties.join();
	}

	/**
	 * Convert the contents currently in the store into a Map. Mainly meant for testing purposes to easily compare
	 * the map contents to expected contents. Not thread safe. Test code is expected to call this at a time
	 * when the work on the map is 'done'.
	 */
	public Map<String,String> asMap() {
		Map<String, String> builder = new HashMap<>();
		for (Entry<Object, Object> e : props.entrySet()) {
			builder.put((String)e.getKey(), (String)e.getValue());
		}
		return Collections.unmodifiableMap(builder);
	}

}
