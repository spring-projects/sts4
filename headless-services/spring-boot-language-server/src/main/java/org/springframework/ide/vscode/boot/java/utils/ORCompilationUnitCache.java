package org.springframework.ide.vscode.boot.java.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser.Input;
import org.openrewrite.Result;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.UpdateSourcePositions;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.Disposable;

public class ORCompilationUnitCache implements DocumentContentProvider, Disposable {
	
	private static final Logger logger = LoggerFactory.getLogger(CompilationUnitCache.class);

	private static final long CU_ACCESS_EXPIRATION = 1;
	private JavaProjectFinder projectFinder;
	private ProjectObserver projectObserver;
	
	private final ProjectObserver.Listener projectListener;
	private final SimpleTextDocumentService documentService;

	private final Cache<URI, CompilationUnit> uriToCu;
	private final Cache<IJavaProject, Set<URI>> projectToDocs;
	private final Cache<IJavaProject, JavaParser> javaParsers;
	
	public ORCompilationUnitCache(JavaProjectFinder projectFinder, SimpleLanguageServer server, ProjectObserver projectObserver) {
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		
		// PT 154618835 - Avoid retaining the CU in the cache as it consumes memory if it hasn't been
		// accessed after some time
		this.uriToCu = CacheBuilder.newBuilder()
				.expireAfterWrite(CU_ACCESS_EXPIRATION, TimeUnit.MINUTES)
				.build();
		this.projectToDocs = CacheBuilder.newBuilder().build();
		this.javaParsers = CacheBuilder.newBuilder().build();

		this.documentService = server == null ? null : server.getTextDocumentService();

		// IMPORTANT ===> these notifications arrive within the lsp message loop, so reactions to them have to be fast
		// and not be blocked by waiting for anything
		if (this.documentService != null) {
			this.documentService.onDidChangeContent(doc -> invalidateCuForJavaFile(doc.getDocument().getId().getUri()));
			this.documentService.onDidClose(doc -> invalidateCuForJavaFile(doc.getId().getUri()));
		}

		if (this.projectFinder != null) {
			for (IJavaProject project : this.projectFinder.all()) {
				logger.info("CU Cache: initial lookup env creation for project <{}>", project.getElementName());
				loadJavaParser(project);
			}
		}

		this.projectListener = new ProjectObserver.Listener() {
			
			@Override
			public void deleted(IJavaProject project) {
				logger.info("CU Cache: deleted project {}", project.getElementName());
				invalidateProject(project);
			}
			
			@Override
			public void created(IJavaProject project) {
				logger.info("CU Cache: created project {}", project.getElementName());
				invalidateProject(project);
				loadJavaParser(project);
			}
			
			@Override
			public void changed(IJavaProject project) {
				logger.info("CU Cache: changed project {}", project.getElementName());
				invalidateProject(project);
				// Load the new cache the value right away
				loadJavaParser(project);
			}
		};

		if (this.projectObserver != null) {
			this.projectObserver.addListener(this.projectListener);
		}
		
	}

	public void dispose() {
		if (this.projectObserver != null) {
			this.projectObserver.removeListener(this.projectListener);
		}
	}


	private JavaParser loadJavaParser(IJavaProject project) {
		try {
			return javaParsers.get(project, () -> {
				List<Path> classpath = getClasspathEntries(project).stream().map(s -> new File(s).toPath()).collect(Collectors.toList());
				JavaParser jp = JavaParser.fromJavaVersion().build();
				jp.setClasspath(classpath);
				return jp;
			});
		} catch (ExecutionException e) {
			logger.error("{}", e);
			return null;
		}
	}
	
	private static Set<String> getClasspathEntries(IJavaProject project) throws Exception {
		if (project == null) {
			return Collections.emptySet();
		} else {
			IClasspath classpath = project.getClasspath();
			Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
			return classpathEntries
					.filter(file -> file.exists())
					.map(file -> file.getAbsolutePath()).collect(Collectors.toSet());
		}
	}
	
	private void invalidateCuForJavaFile(String uriStr) {
		logger.info("CU Cache: invalidate AST for {}", uriStr);

		URI uri = URI.create(uriStr);
		uriToCu.invalidate(uri);
		JavaParser parser = javaParsers.getIfPresent(uri);
		if (parser != null) {
			parser.reset();
		}
	}

	private void invalidateProject(IJavaProject project) {
		logger.info("CU Cache: invalidate project <{}>", project.getElementName());

		Set<URI> docUris = projectToDocs.getIfPresent(project);
		if (docUris != null) {
			uriToCu.invalidateAll(docUris);
			projectToDocs.invalidate(project);
		}
		javaParsers.invalidate(project);
	}

	@Override
	public String fetchContent(URI uri) throws Exception {
		if (documentService != null) {
			TextDocument document = documentService.getLatestSnapshot(uri.toString());
			if (document != null) {
				return document.get();
			}
		}
		return IOUtils.toString(uri);
	}
	
	/**
	 * Never research shows at the AST is thread-safe when used in read-only mode:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=58314
	 * 
	 * This means that the previous implemented synchronization around the requestor
	 * working on the AST is not necessary as long as the requestor operates in read-only
	 * mode on the AST nodes.
	 * 
	 * Warning: Callers should take care to do all AST processing inside of the requestor callback and
	 * not pass of AST nodes to helper functions that work aynchronously or store AST nodes or ITypeBindings
	 * for later use. The JDT ASTs are not thread safe!
	 */
	public <T> T withCompilationUnit(IJavaProject project, URI uri, Function<CompilationUnit, T> requestor) {
		logger.info("CU Cache: work item submitted for doc {}", uri.toString());

		if (project != null) {

			CompilationUnit cu = null;

			try {
				cu = uriToCu.get(uri, () -> {
					JavaParser javaParser = loadJavaParser(project);
					Input input = new Input(Paths.get(uri), () -> {
						try {
							return new ByteArrayInputStream(fetchContent(uri).getBytes());
						} catch (Exception e) {
							throw new IllegalStateException("Unexpected error fetching document content");
						}
					});
					
					logger.info("CU Cache: created new AST for {}", uri.toString());
					
					return ORAstUtils.parseInputs(javaParser, List.of(input)).get(0);
										
				});

				if (cu != null) {
					projectToDocs.get(project, () -> new HashSet<>()).add(uri);
				}

			} catch (Exception e) {
				logger.error("", e);
			}

			if (cu != null) {
				try {
					logger.info("CU Cache: start work on AST for {}", uri.toString());
					return requestor.apply(cu);
				}
				catch (CancellationException e) {
					throw e;
				}
				catch (Exception e) {
					logger.error("", e);
				}
				finally {
					logger.info("CU Cache: end work on AST for {}", uri.toString());
				}
			}
		}

		return requestor.apply(null);
	}
	
	/**
	 * Never research shows at the AST is thread-safe when used in read-only mode:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=58314
	 * 
	 * This means that the previous implemented synchronization around the requestor
	 * working on the AST is not necessary as long as the requestor operates in read-only
	 * mode on the AST nodes.
	 * 
	 * Warning: Callers should take care to do all AST processing inside of the requestor callback and
	 * not pass of AST nodes to helper functions that work aynchronously or store AST nodes or ITypeBindings
	 * for later use. The JDT ASTs are not thread safe!
	 */
	@Deprecated
	public <T> T withCompilationUnit(TextDocument document, Function<CompilationUnit, T> requestor) {
		IJavaProject project = this.projectFinder != null ? projectFinder.find(document.getId()).orElse(null) : null;

		URI uri = URI.create(document.getUri());
		return withCompilationUnit(project, uri, requestor);
	}


}
