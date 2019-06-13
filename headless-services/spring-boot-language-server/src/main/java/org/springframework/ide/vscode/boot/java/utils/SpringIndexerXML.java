/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXML implements SpringIndexer {

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	private final SymbolHandler symbolHandler;
	private final Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler;
	private final SymbolCache cache;
	private final JavaProjectFinder projectFinder;
	
	private String[] scanFolderGlobs = new String[0];

	public SpringIndexerXML(SymbolHandler handler, Map<String, SpringIndexerXMLNamespaceHandler> namespaceHandler,
			SymbolCache cache, JavaProjectFinder projectFinder) {
		this.symbolHandler = handler;
		this.namespaceHandler = namespaceHandler;
		this.cache = cache;
		this.projectFinder = projectFinder;
	}

	public void setScanFolderGlobs(String[] scanFolderGlobs) {
		if (!Arrays.equals(this.scanFolderGlobs, scanFolderGlobs)) {
			clearIndex();
			this.scanFolderGlobs = scanFolderGlobs;
			populateIndex();
		}
	}

	@Override
	public String[] getFileWatchPatterns() {
		String[] patterns = new String[scanFolderGlobs.length];
		for (int i = 0; i < scanFolderGlobs.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(scanFolderGlobs[i]);
			if (scanFolderGlobs[i].charAt(scanFolderGlobs[i].length() - 1) != '/') {
				sb.append('/');
			}
			sb.append("*.xml");
			patterns[i] = sb.toString();
		}
		return patterns;
	}

	@Override
	public boolean isInterestedIn(String docURI) {
		return docURI.endsWith(".xml");
	}

	@Override
	public void initializeProject(IJavaProject project) throws Exception {
		String[] files = this.getFiles(project);

		log.info("scan xml files for symbols for project: " + project.getElementName() + " - no. of files: " + files.length);

		long startTime = System.currentTimeMillis();
		SymbolCacheKey cacheKey = getCacheKey(project);

		CachedSymbol[] symbols = this.cache.retrieveSymbols(cacheKey, files);
		if (symbols == null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();

			for (String file : files) {
				scanFile(project, file, generatedSymbols);
			}

			this.cache.store(cacheKey, files, generatedSymbols, null);

			symbols = (CachedSymbol[]) generatedSymbols.toArray(new CachedSymbol[generatedSymbols.size()]);
		}
		else {
			log.info("scan xml files used cached data: " + project.getElementName() + " - no. of cached symbols retrieved: " + symbols.length);
		}

		if (symbols != null) {
			for (int i = 0; i < symbols.length; i++) {
				CachedSymbol symbol = symbols[i];
				symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
			}
		}

		long endTime = System.currentTimeMillis();

		log.info("scan xml files for symbols for project: " + project.getElementName() + " took ms: " + (endTime - startTime));
	}

	@Override
	public void removeProject(IJavaProject project) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		this.cache.remove(cacheKey);
	}

	@Override
	public void updateFile(IJavaProject project, String docURI, long lastModified, Supplier<String> content) throws Exception {

		List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();

		scanFile(project, content.get(), docURI, lastModified, generatedSymbols);

		SymbolCacheKey cacheKey = getCacheKey(project);
		String file = new File(new URI(docURI)).getAbsolutePath();
		this.cache.update(cacheKey, file, lastModified, generatedSymbols, null);

		for (CachedSymbol symbol : generatedSymbols) {
			symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
		}
	}

	@Override
	public void removeFile(IJavaProject project, String docURI) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		String file = new File(new URI(docURI)).getAbsolutePath();
		this.cache.removeFile(cacheKey, file);
	}

	private void scanFile(IJavaProject project, String fileName, List<CachedSymbol> generatedSymbols) {
		log.debug("starting to parse XML file for Spring symbol indexing: {}", fileName);

		try {
			File file = new File(fileName);
			long lastModified = file.lastModified();

			String docURI = UriUtil.toUri(file).toString();
			String fileContent = FileUtils.readFileToString(file);

	        scanFile(project, fileContent, docURI, lastModified, generatedSymbols);
		}
		catch (Exception e) {
			log.error("error parsing XML file: ", e);
		}
	}

	private void scanFile(IJavaProject project, String fileContent, String docURI, long lastModified, List<CachedSymbol> generatedSymbols) throws Exception {
		DOMParser parser = DOMParser.getInstance();
		DOMDocument document = parser.parse(fileContent, "", null);

		AtomicReference<TextDocument> docRef = new AtomicReference<>();
		scanNode(document, project, docURI, lastModified, docRef, fileContent, generatedSymbols);
	}

	private void scanNode(DOMNode node, IJavaProject project, String docURI, long lastModified, AtomicReference<TextDocument> docRef, String content, List<CachedSymbol> generatedSymbols) throws Exception {
		String namespaceURI = node.getNamespaceURI();

		if (namespaceURI != null && this.namespaceHandler.containsKey(namespaceURI)) {
			SpringIndexerXMLNamespaceHandler namespaceHandler = this.namespaceHandler.get(namespaceURI);

			TextDocument document = DocumentUtils.getTempTextDocument(docURI, docRef, content);
			namespaceHandler.processNode(node, project, docURI, lastModified, document, generatedSymbols);
		}


//		if ("http://www.springframework.org/schema/beans".equals(namespaceURI)) {
//			List<DOMAttr> attributeNodes = node.getAttributeNodes();
//			if (attributeNodes != null) {
//				for (DOMAttr attribute : attributeNodes) {
//					System.out.println(attribute.getName() + " - " + attribute.getValue());
//				}
//			}
//		}

		List<DOMNode> children = node.getChildren();
		for (DOMNode child : children) {
			scanNode(child, project, docURI, lastModified, docRef, content, generatedSymbols);
		}


	}

	private String[] getFiles(IJavaProject project) throws Exception {
		String[] globs = scanFolderGlobs;
		if (globs.length == 0) {
			return new String[0];
		}
		List<PathMatcher> matchers = new ArrayList<>(globs.length);
		for (String glob : globs) {
			matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + glob));
		}
		
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		Files.walkFileTree(Paths.get(project.getLocationUri()), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String fileName = file.getFileName().toString();
				if (fileName.endsWith(".xml")) {
					Path parent = file.getParent();
					if (parent != null) {
						for (PathMatcher matcher : matchers) {
							if (matcher.matches(parent)) {
								builder.add(file.toAbsolutePath().toString());
							}
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});

		ImmutableList<String> list = builder.build();
		return list.toArray(new String[list.size()]);
	}

	private SymbolCacheKey getCacheKey(IJavaProject project) {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();

		String classpathIdentifier = classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath() + "#" + file.lastModified())
				.collect(Collectors.joining(","));

		return new SymbolCacheKey(project.getElementName() + "-xml-", DigestUtils.md5Hex(classpathIdentifier).toUpperCase());
	}
	
	private void clearIndex() {
		for (IJavaProject project : projectFinder.all()) {
			try {
				for (String file : getFiles(project)) {
					String docUri = UriUtil.toUri(new File(file)).toString();
					symbolHandler.removeSymbols(project, docUri);
					removeFile(project, docUri);
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
	
	private void populateIndex() {
		for (IJavaProject project : projectFinder.all()) {
			try {
				initializeProject(project);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
	
}
