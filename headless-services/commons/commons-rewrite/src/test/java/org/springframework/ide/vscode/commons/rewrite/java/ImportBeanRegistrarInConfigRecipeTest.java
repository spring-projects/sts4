/*******************************************************************************
 * Copyright (c) 2025 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ImportBeanRegistrarInConfigRecipeTest implements RewriteTest {
	
	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new ImportBeanRegistrarInConfigRecipe("com.example.test.Config", "java.util.Date"))
				.parser(JavaParser.fromJavaVersion().classpath("spring-beans", "spring-context"));
	}
	
	@Test
	void addImportAnnotation() {
		rewriteRun(
			java(
				"""
				package com.example.test;
				
				class Config {
				}
				""",
				"""
				package com.example.test;
				
				import org.springframework.context.annotation.Import;
				
				import java.util.Date;
				
				@Import(Date.class)
				class Config {
				}
				"""
			)
		);
	}

	@Test
	void addImportAnnotationNextToExistingAnnotation() {
		rewriteRun(
			java(
				"""
				package com.example.test;
								
				@Deprecated
				class Config {
				}
				""",
				"""
				package com.example.test;
				
				import org.springframework.context.annotation.Import;
				
				import java.util.Date;
				
				@Deprecated
				@Import(Date.class)
				class Config {
				}
				"""
			)
		);
	}

	@Test
	void turnIntoArray() {
		rewriteRun(
			java(
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					@Import(String.class)
					class Config {
					}
					""",
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					import java.util.Date;
					
					@Import({String.class, Date.class})
					class Config {
					}
					"""
			)
		);
	}

	@Test
	void addEntryToArray() {
		rewriteRun(
			java(
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					@Import({String.class})
					class Config {
					}
					""",
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					import java.util.Date;
					
					@Import({String.class, Date.class})
					class Config {
					}
					"""
			)
		);
	}
	
	@Test
	void turnIntoArray_Value() {
		rewriteRun(
			java(
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					@Import(value = String.class)
					class Config {
					}
					""",
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					import java.util.Date;
					
					@Import(value = {String.class, Date.class})
					class Config {
					}
					"""
			)
		);
	}

	@Test
	void addEntryIntoArray_Value() {
		rewriteRun(
			java(
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					@Import(value = {String.class})
					class Config {
					}
					""",
					"""
					package com.example.test;
					
					import org.springframework.context.annotation.Import;
					
					import java.util.Date;
					
					@Import(value = {String.class, Date.class})
					class Config {
					}
					"""
			)
		);
	}

}
