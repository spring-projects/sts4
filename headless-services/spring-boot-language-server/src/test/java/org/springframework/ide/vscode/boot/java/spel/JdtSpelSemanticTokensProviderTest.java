/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.spel;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.reconcilers.CompositeASTVisitor;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class JdtSpelSemanticTokensProviderTest {

	@Autowired JdtSpelSemanticTokensProvider provider;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	private MavenJavaProject jp;
		
	@BeforeEach
	public void setup() throws Exception {
		jp =  projects.mavenProject("spring-modulith-example-full");
	}
	
	private List<SemanticTokenData> computeTokens(CompilationUnit cu) {
		Collector<SemanticTokenData> collector = new Collector<>();
		CompositeASTVisitor visitor = new CompositeASTVisitor();
		visitor.add(provider.getTokensComputer(jp, null, cu, collector));
		cu.accept(visitor);
		return collector.get();
	}

	@Test
	void normalAnnotation() throws Exception {
		String source = """
		package my.package
		
		import org.springframework.beans.factory.annotation.Value;
		
		public class Owner {

			@Value(value="#{new String('hello world').toUpperCase()}")
			String s;
			
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = computeTokens(cu);
        
        assertThat(tokens.size()).isEqualTo(9);
        
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(119, 122, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("new");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(123, 129, "method", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("String");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(129, 130, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("(");
        
        token = tokens.get(3);
        assertThat(token).isEqualTo(new SemanticTokenData(130, 143, "string", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("'hello world'");

        token = tokens.get(4);
        assertThat(token).isEqualTo(new SemanticTokenData(143, 144, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo(")");

        token = tokens.get(5);
        assertThat(token).isEqualTo(new SemanticTokenData(144, 145, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo(".");

        token = tokens.get(6);
        assertThat(token).isEqualTo(new SemanticTokenData(145, 156, "method", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("toUpperCase");

        token = tokens.get(7);
        assertThat(token).isEqualTo(new SemanticTokenData(156, 157, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("(");

        token = tokens.get(8);
        assertThat(token).isEqualTo(new SemanticTokenData(157, 158, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo(")");

	}
 
	@Test
	void singleMemberAnnotation() throws Exception {
		String source = """
		package my.package
		
		import org.springframework.beans.factory.annotation.Value;
		
		public class Owner {

			@Value("#{'invalid alphabetic string #$1' matches '[a-zA-Z\\s]+' }")
			String s;
			
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = computeTokens(cu);
        
        assertThat(tokens.size()).isEqualTo(3);
        
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(113, 144, "string", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("'invalid alphabetic string #$1'");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(145, 152, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("matches");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(153, 166, "string", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("'[a-zA-Z\\s]+'");
        
	}
	
	@Test
	void leadingAndTrailingSpaces() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="  #{demo.cron} ")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(3);
		
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(121, 125, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("demo");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(125, 126, "operator", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo(".");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(126, 130, "property", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("cron");
	}

}
