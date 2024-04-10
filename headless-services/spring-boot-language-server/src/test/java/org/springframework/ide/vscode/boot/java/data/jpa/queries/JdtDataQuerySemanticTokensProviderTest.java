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
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

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
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class JdtDataQuerySemanticTokensProviderTest {
	
	@Autowired JdtDataQuerySemanticTokensProvider provider;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	private MavenJavaProject jp;
		
	@BeforeEach
	public void setup() throws Exception {
		jp =  projects.mavenProject("spring-modulith-example-full");
	}

	@Test
	void singleMemberAnnotation() throws Exception {
		String source = """
		package my.package
		
		import org.springframework.data.jpa.repository.Query;
		
		public interface OwnerRepository {
		
			@Query("SELECT DISTINCT owner FROM Owner owner")
			void findByLastName();
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = provider.computeTokens(cu);
        
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(120, 126, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("SELECT");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(127, 135, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("DISTINCT");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(136, 141, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
        
        token = tokens.get(3);
        assertThat(token).isEqualTo(new SemanticTokenData(142, 146, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("FROM");

        token = tokens.get(4);
        assertThat(token).isEqualTo(new SemanticTokenData(147, 152, "class", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("Owner");

        token = tokens.get(5);
        assertThat(token).isEqualTo(new SemanticTokenData(153, 158, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
	}

	@Test
	void normalAnnotation() throws Exception {
		String source = """
		package my.package
		
		import org.springframework.data.jpa.repository.Query;
		
		public interface OwnerRepository {
		
			@Query(value = "SELECT DISTINCT owner FROM Owner owner")
			void findByLastName();
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = provider.computeTokens(cu);
        
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(128, 134, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("SELECT");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(135, 143, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("DISTINCT");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(144, 149, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
        
        token = tokens.get(3);
        assertThat(token).isEqualTo(new SemanticTokenData(150, 154, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("FROM");

        token = tokens.get(4);
        assertThat(token).isEqualTo(new SemanticTokenData(155, 160, "class", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("Owner");

        token = tokens.get(5);
        assertThat(token).isEqualTo(new SemanticTokenData(161, 166, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
	}
	
	@Test
	void createQueryMethod() throws Exception {
		String source = """
		package my.package
		
		import jakarta.persistence.EntityManager;
		
		public interface OwnerRepository {
		
			default void findByLastName(EntityManager manager) {
				manager.createQuery("SELECT DISTINCT owner FROM Owner owner")
			}
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = provider.computeTokens(cu);
        
        SemanticTokenData token = tokens.get(0);
        assertThat(token).isEqualTo(new SemanticTokenData(176, 182, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("SELECT");
        
        token = tokens.get(1);
        assertThat(token).isEqualTo(new SemanticTokenData(183, 191, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("DISTINCT");
        
        token = tokens.get(2);
        assertThat(token).isEqualTo(new SemanticTokenData(192, 197, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
        
        token = tokens.get(3);
        assertThat(token).isEqualTo(new SemanticTokenData(198, 202, "keyword", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("FROM");

        token = tokens.get(4);
        assertThat(token).isEqualTo(new SemanticTokenData(203, 208, "class", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("Owner");

        token = tokens.get(5);
        assertThat(token).isEqualTo(new SemanticTokenData(209, 214, "variable", new String[0]));
        assertThat(source.substring(token.start(), token.end())).isEqualTo("owner");
	}

	@Test
	void nativeQuery() throws Exception {
		String source = """
		package my.package
		
		import org.springframework.data.jpa.repository.Query;
		
		public interface OwnerRepository {
		
			@Query(value = "SELECT DISTINCT owner FROM Owner owner", nativeQuery = true)
			void findByLastName();
		}
		""";
        
        String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/OwnerRepository.java").toUri().toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "OwnerRepository.java", jp);
        
        assertThat(cu).isNotNull();
        
        List<SemanticTokenData> tokens = provider.computeTokens(cu);
        
        assertThat(tokens.size()).isZero();
        
	}

}
