/*******************************************************************************
 * Copyright (c) 2017, 2025 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeRun;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class AddFieldRecipeTest implements RewriteTest {
	
	@Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddFieldRecipe("com.example.test.OwnerRepository", "com.example.demo.FooBar", null))
        .parser(JavaParser.fromJavaVersion()
        		.logCompilationWarningsAndErrors(true));
    }
	
	public static void runRecipeAndAssert(Recipe recipe, String beforeSourceStr, String expectedSourceStr, String dependsOn) {
        JavaParser javaParser = JavaParser.fromJavaVersion().dependsOn(dependsOn).build();

        List<SourceFile> list = javaParser.parse(beforeSourceStr).toList();
        SourceFile beforeSource = list.get(0);

        assertThat(beforeSource.printAll()).isEqualTo(beforeSourceStr);

        InMemoryLargeSourceSet ss = new InMemoryLargeSourceSet(list);
        RecipeRun recipeRun = recipe.run(ss, new InMemoryExecutionContext(t -> {
            throw new RuntimeException(t);
        }));
        org.openrewrite.Result res = recipeRun.getChangeset().getAllResults().get(0);
        assertThat(res.getAfter().printAll()).isEqualTo(expectedSourceStr);
    }
	
	// The test parses invalid LST and then applies the recipe
	@Test
	void addField() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                class FooBar {
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;
		
        String expectedSourceStr = """
                package com.example.demo;
                
                class FooBar {
                
                    private final OwnerRepository ownerRepository;
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;

        String dependsOn = """
                package com.example.demo;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.demo.OwnerRepository", "com.example.demo.FooBar", null);
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
	} 
	
	@Test
	void addFieldAndImport() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                class FooBar {
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;
		
        String expectedSourceStr = """
                package com.example.demo;
                
            import com.example.test.OwnerRepository;
                
            class FooBar {
                
                    private final OwnerRepository ownerRepository;
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;

        String dependsOn = """
                package com.example.test;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.test.OwnerRepository", "com.example.demo.FooBar", null);
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
	} 
	
	@Test
    void addNestedField() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                class FooBar {
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;
		
        String expectedSourceStr = """
                package com.example.demo;
                
            import com.example.test.Inner.OwnerRepository;
                
            class FooBar {
                
                    private final Inner.OwnerRepository ownerRepository;
                    
                    public void test() {
                        ownerR

                    }
                
                }
            """;

        String dependsOn = """
                package com.example.test;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.test.Inner.OwnerRepository", "com.example.demo.FooBar", null);
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
    }
	
	@Test
    void addToNestedComponent() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                class FooBar {
                    class Inner {
                    
                        public void test() {
                            ownerR
                        }
                    }
                
                }
            """;
		
        String expectedSourceStr = """
    package com.example.demo;

import com.example.test.OwnerRepository;

class FooBar {
        class Inner {
        
            private final OwnerRepository ownerRepository;
            
            public void test() {
                ownerR
            }
        }

    }
            """;

        String dependsOn = """
                package com.example.test;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.test.OwnerRepository", "com.example.demo.FooBar$Inner", null);
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
    }
	
	@Test
    void addFieldToFirstClass() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                class FooBar {
                    
                    public void test() {
                        ownerR

                    }
                
                }
                class FooBarNew {
                    
                    public void test1() {}
                
                }
            """;
		
        String expectedSourceStr = """
                package com.example.demo;
                
            import com.example.test.Inner.OwnerRepository;
                
            class FooBar {
                
                    private final Inner.OwnerRepository ownerRepository;
                    
                    public void test() {
                        ownerR

                    }
                
                }
                class FooBarNew {
                    
                    public void test1() {}
                
                }
            """;

        String dependsOn = """
                package com.example.test;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.test.Inner.OwnerRepository", "com.example.demo.FooBar", null);
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
    }
	
	@Test
    void addFieldToSecondClass() {
		
		String beforeSourceStr = """
                package com.example.demo;
                
                import org.springframework.stereotype.Component;
                
                @Component
                class FooBar {
                    
                    public void test() {
                        ownerR

                    }
                
                }
                @Component
                class FooBarNew {
                    
                    public void test1() {}
                
                }
            """;
		
        String expectedSourceStr = """
    package com.example.demo;

    import com.example.test.Inner.OwnerRepository;
import org.springframework.stereotype.Component;

@Component
    class FooBar {

        public void test() {
            ownerR

        }

    }
    @Component
    class FooBarNew {

        private final Inner.OwnerRepository ownerRepo;

        public void test1() {}

    }
            """;

        String dependsOn = """
                package com.example.test;
                public interface OwnerRepository{}
            """;

        Recipe recipe = new AddFieldRecipe("com.example.test.Inner.OwnerRepository", "com.example.demo.FooBarNew", "ownerRepo");
        runRecipeAndAssert(recipe, beforeSourceStr, expectedSourceStr, dependsOn);
    }

}
