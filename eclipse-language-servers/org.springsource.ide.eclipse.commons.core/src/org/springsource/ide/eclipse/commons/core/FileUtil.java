/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;


/**
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Kris De Volder
 */
public class FileUtil {

	private static final int MAX_FILE_SIZE = 1024 * 1024;

	public static final int BUFFER_SIZE = 4096;

	public static void copyDirectory(File sourceDirectory, File targetDirectory, IProgressMonitor monitor)
			throws CoreException {
		File[] files = sourceDirectory.listFiles();
		if (files != null) {
			for (File sourceFile : files) {
				File targetFile = new File(targetDirectory, sourceFile.getName());
				if (sourceFile.isDirectory()) {
					targetFile.mkdir();
					copyDirectory(sourceFile, targetFile, monitor);
				}
				else {
					copyFile(sourceFile, targetFile, monitor);
				}
			}
		}
	}

	public static void copyFile(File sourceFile, File targetFile, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("Copying file", (int) sourceFile.length());

			FileInputStream in = new FileInputStream(sourceFile);
			try {
				writeToFile(in, targetFile, monitor);
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Could not copy file from "
					+ sourceFile + " to " + targetFile, e));
		}
		finally {
			monitor.done();
		}
	}

	public static void writeToFile(InputStream in, File targetFile, IProgressMonitor monitor)
			throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(targetFile);
		try {
			int len;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((len = in.read(buffer)) > 0) {
				Policy.checkCancelled(monitor);

				out.write(buffer, 0, len);
				monitor.worked(len);
			}
		}
		finally {
			out.close();
		}
	}

	public static File getFile(Bundle bundle, String filename) throws CoreException {
		URL url = bundle.getResource(filename);
		if (url == null) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Could not locate file: "
					+ filename, new FileNotFoundException()));
		}

		try {
			return new File(FileLocator.toFileURL(url).getPath());
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Could not locate file: "
					+ filename, e));
		}
	}

	public static File getFile(Plugin plugin, String filename) throws CoreException {
		return getFile(plugin.getBundle(), filename);
	}

	public static String readFile(File sourceFile, IProgressMonitor monitor) throws CoreException {
		long length = sourceFile.length();
		if (length > MAX_FILE_SIZE) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "File \"" + sourceFile
					+ "\" is too large"));
		}

		try {
			monitor.beginTask("Reading file", (int) length);

			FileReader in = new FileReader(sourceFile);
			try {
				StringBuilder sb = new StringBuilder((int) length);
				int len;
				char[] buffer = new char[BUFFER_SIZE];
				while ((len = in.read(buffer)) > 0) {
					Policy.checkCancelled(monitor);

					sb.append(buffer, 0, len);
					monitor.worked(len);
				}
				return sb.toString();
			}
			finally {
				in.close();
			}
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Could not read file \""
					+ sourceFile + "\"", e));
		}
		finally {
			monitor.done();
		}
	}

	private static String[] BINARY_EXTENSIONS = new String[] { "jar", "gif", "jpg", "jpeg", ".class", "png" };

	public static void copy(File source, File target) throws IOException {
		FileInputStream sourceOutStream = new FileInputStream(source);
		FileOutputStream targetOutStream = new FileOutputStream(target);
		FileChannel sourceChannel = sourceOutStream.getChannel();
		FileChannel targetChannel = targetOutStream.getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
		sourceChannel.close();
		targetChannel.close();
		sourceOutStream.close();
		targetOutStream.close();
	}

	public static boolean isBinaryFile(File file) {
		String extension = FileUtil.getExtension(file);
		if (extension != null) {
			for (String binaryExtension : BINARY_EXTENSIONS) {
				if (binaryExtension.equals(extension)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getExtension(File file) {
		String fileName = file.getName();
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex == -1) {
			return null;
		}
		return fileName.substring(extensionIndex + 1);
	}

}
