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
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Various Java installation utility methods
 *
 * @author Alex Boyko
 *
 */
public class JavaUtils {

	private static Logger log = LoggerFactory.getLogger(JavaUtils.class);

	/**
	 * Find JRE libs jars
	 *
	 * @param javaMinorVersionSupplier
	 * @param javaHomeSupplier
	 * @param bootClasspathSupplier
	 * @return
	 */
	public static Stream<Path> jreLibs(Supplier<String> javaMinorVersionSupplier, Supplier<String> javaHomeSupplier, Supplier<String> bootClasspathSupplier) {
		String versionString = javaMinorVersionSupplier.get();
		try {
			int version = versionString == null ? 8 : Integer.valueOf(versionString);
			if (version > 8) {
				String javaHome = javaHomeSupplier.get();
				if (javaHome != null) {
					Path rtPath= Paths.get(javaHome, "lib", "jrt-fs.jar");
					if (Files.exists(rtPath)) {
						return Stream.of(rtPath);
					} else {
						log.error("Cannot find file " + rtPath);
					}
				}
			} else {
				String s = bootClasspathSupplier.get();
				if (s != null) {
					return  Arrays.stream(s.split(File.pathSeparator)).map(File::new).filter(f -> f.canRead()).map(f -> Paths.get(f.toURI()));
				}
			}
		} catch (NumberFormatException e) {
			log.error("Cannot extract java minor version number.", e);
		}
		return Stream.empty();
	}

	/**
	 * Extracts java version string from full version string, i.e. "8" from 1.8.0_151, "9" from 9.0.1, "10" from 10.0.1
	 *
	 * @param fullVersion
	 * @return
	 */
	public static String getJavaRuntimeMinorVersion(String fullVersion) {
		String[] tokenized = fullVersion.split("\\.");
		if (tokenized[0] == "1") {
			if (tokenized.length > 1) {
				return tokenized[1];
			} else {
				log.error("Cannot determine minor version for the Java Runtime Version: " + fullVersion);
				return null;
			}
		} else {
			String version = tokenized[0];
			int idx = version.indexOf('+');
			return idx >= 0 ? version.substring(0, idx) : version;
		}
	}

	public static Path javaHomeFromLibJar(Path libJar) {
		Path root = libJar.getRoot();
		for (Path home = libJar; !root.equals(home.getParent()); home = home.getParent()) {
			Path bin = home.resolve("bin");
			Path lib = home.resolve("lib");
			Path include = home.resolve("include");
			Path man = home.resolve("man");
			Path legal = home.resolve("legal");
			if (Files.isDirectory(bin) && Files.isDirectory(lib) && Files.isDirectory(include) && (Files.isDirectory(man) || Files.isDirectory(legal))) {
				return home;
			}
		}
		return null;
	}

	public static Path jreSources(Path libJar) {
		Path home = javaHomeFromLibJar(libJar);
		if (home != null) {
			Path sources = JavaUtils.sourceZip(home);
			if (sources == null) {
				sources = JavaUtils.sourceZip(home.resolve("lib"));
			}
			return sources;
		}
		return null;
	}

	private static Path sourceZip(Path containerFolder) {
		Path sourcesZip = containerFolder.resolve("src.zip");
		if (Files.exists(sourcesZip)) {
			return sourcesZip;
		}
		return null;
	}

	public static String typeBindingKeyToFqName(String bindingKey) {
		return bindingKey == null ? null : bindingKey.substring(1, bindingKey.length() - 1).replace('/', '.');
	}

	public static String typeFqNametoBindingKey(String fqName) {
		StringBuilder sb = new StringBuilder('L');
		sb.append(fqName.replace('.', '/'));
		sb.append(';');
		return sb.toString();
	}

}
