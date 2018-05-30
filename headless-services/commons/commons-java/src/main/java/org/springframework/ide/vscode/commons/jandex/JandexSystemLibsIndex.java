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
package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.io.IOException;
import java.nio.channels.IllegalSelectorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.io.BaseEncoding;

/**
 * Keeps cache of system libs Jandex Indexes
 *
 * @author Alex Boyko
 *
 */
public class JandexSystemLibsIndex {

	private static final String DEFAULT_JAVA_VERSION = "1.8.0";

	private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("^java version \"(.*)\"$");

	public static final Logger log = LoggerFactory.getLogger(JandexSystemLibsIndex.class);

	private static final Supplier<JandexSystemLibsIndex> INSTANCE = Suppliers.memoize(() -> new JandexSystemLibsIndex());

	private Cache<Path, BasicJandexIndex> cache;

	private JandexSystemLibsIndex() {
		this.cache = CacheBuilder.newBuilder().build(new CacheLoader<Path, BasicJandexIndex>() {

			@Override
			public BasicJandexIndex load(Path key) throws Exception {
				return createIndex(key);
			}

		});
	}

	/**
	 * Retrieves or lazily creates Jandex Index for a folder containing system lib jars
	 * @param path the path containing jars
	 * @return Jandex Index of the jars contained in the folder
	 */
	public BasicJandexIndex index(Path path) {
		try {
			return cache.get(path, () -> createIndex(path));
		} catch (ExecutionException e) {
			log.error("Failed to detrmine Jandex index for " + path, e);
			return null;
		}
	}

	/**
	 * Retrieves Jandex Indexes appropriate for systm lib jars. One Jandex Index may contain all sys lib jars.
	 * @param jars system lib jars
	 * @return Jandex Indexs for jars
	 */
	public BasicJandexIndex[] fromJars(Collection<File> jars) {
		return jars.stream().map(jar -> jar.toPath().getParent()).distinct().map(folder -> index(folder)).filter(Objects::nonNull).toArray(BasicJandexIndex[]::new);
	}

	public static JandexSystemLibsIndex getInstance() {
		return INSTANCE.get();
	}

	private BasicJandexIndex createIndex(Path path) {
		List<File> jars = Collections.emptyList();
		try {
			jars = Files.list(path).filter(p -> p.getFileName().toString().endsWith(".jar") && Files.isRegularFile(p)).map(p -> p.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			// Shouldn't happen - there should at least be one jar file
			log.error("Cannot list files in folder " + path, e);
		}
		return new BasicJandexIndex(jars, jarFile -> findIndexFile(jarFile));
	}

	private File findIndexFile(File jarFile) {
		return Paths.get(System.getProperty("user.home"), ".sts4-jandex", folderNameforPath(jarFile.getParentFile().toString()), jarFile.getName() + ".jdx").toFile();
	}

	private static String folderNameforPath(String path) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] bytes = md.digest((path).getBytes());
			String name = new String(BaseEncoding.base32().encode(bytes));
			name = name.replace('/', '_'); //slashes are trouble in file names.
			return name;
		} catch (NoSuchAlgorithmException e) {
			// shouldn't happen!
			throw new IllegalSelectorException();
		}
	}

//	private static String getJavaVersion(Path path) {
//		// Find valid /bin folder
//		for (; path != null && !(Files.isDirectory(path.resolve("bin")) && Files.isReadable(path.resolve("bin"))); path = path.getParent());
//		// If found, assume it's the java home bin folder
//		if (path != null) {
//			Path javaBin = path.resolve("bin");
//			try {
//				Process p = new ProcessBuilder().directory(javaBin.toFile()).command("./java", "-version").start();
//				BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//				int exitCode = p.waitFor();
//				if (exitCode == 0) {
//					return buffer.lines().map(l -> JAVA_VERSION_PATTERN.matcher(l)).filter(m -> m.find()).findFirst().map(m -> m.group(1)).orElse(DEFAULT_JAVA_VERSION);
//				} else {
//					log.error("Failed to compute java version in folder: " + javaBin + ". 'java -version' exit code is " + exitCode);
//				}
//			} catch (IOException | InterruptedException e) {
//				log.error("Failed to compute java version in folder: " + javaBin, e);
//			}
//		}
//		return DEFAULT_JAVA_VERSION;
//	}
//
//	private static String extractVersionForJavadoc(String javaVersion) {
//		if (javaVersion.startsWith("1.")) {
//			int idx = javaVersion.indexOf('.', 2);
//			return idx >= 0 ? javaVersion.substring(2, idx) : javaVersion.substring(2);
//		} else {
//			int idx = javaVersion.indexOf('.');
//			return idx >= 0 ? javaVersion.substring(0, idx) : javaVersion;
//		}
//	}

}
