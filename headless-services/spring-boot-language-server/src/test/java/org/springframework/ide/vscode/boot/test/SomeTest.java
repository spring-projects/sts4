package org.springframework.ide.vscode.boot.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;


public class SomeTest {
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	private MavenJavaProject jp;

	@Before
	public void setup() throws Exception {
		jp =  projects.mavenProject("empty-boot-15-web-app");
		assertTrue(jp.getIndex().findType("org.springframework.boot.SpringApplication").exists());
	}

	@Test
	public void test1() throws Exception {
		URL sourceUrl = SourceLinks.source(jp, "org.springframework.boot.SpringApplication").get();
		
		URI uri = sourceUrl.toURI();
		
		String unitName = "SpringApplication";
		
		char[] content = IOUtils.toString(uri).toCharArray();
		
		CompilationUnit cu = CompilationUnitCache.parse2(content, uri.toString(), unitName, getClasspathEntries(jp), null);
		
		System.out.println(cu);
	}
	
//	@Test
//	public void test2() throws Exception {
//		URL sourceUrl = SourceLinks.source(jp, "org.springframework.boot.SpringApplication").get();
//		
//		URI uri = sourceUrl.toURI();
//		
//		String unitName = "SpringApplication";
//		
//		char[] content = IOUtils.toString(uri).toCharArray();
//		
//		CompilationUnit cu = CompilationUnitCache.parse(content, uri.toString(), unitName, getClasspathEntries(jp));
//		
//		System.out.println(cu);
//	}
	
	private static String[] getClasspathEntries(IJavaProject project) throws Exception {
		if (project == null) {
			return new String[0];
		} else {
			IClasspath classpath = project.getClasspath();
			Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
			return classpathEntries
					.filter(file -> file.exists())
					.map(file -> file.getAbsolutePath()).toArray(String[]::new);
		}
	}


}
