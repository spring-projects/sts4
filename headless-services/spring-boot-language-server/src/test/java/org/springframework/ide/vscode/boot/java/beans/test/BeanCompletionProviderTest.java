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
package org.springframework.ide.vscode.boot.java.beans.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;


/**
 * @author Udayani V
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class BeanCompletionProviderTest {
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringMetamodelIndex springIndex;
	@Autowired private SpringSymbolIndex indexer;

	private File directory;
	private IJavaProject project;
	private Bean[] indexedBeans;
	private String tempJavaDocUri;
	private Bean bean1;
	private Bean bean2;
	private Bean bean3;
	private Bean bean4;
	private Bean bean5;
	private Bean bean6;
	
	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-indexing/").toURI());

		String projectDir = directory.toURI().toString();
		project = projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
		
		indexedBeans = springIndex.getBeansOfProject(project.getElementName());
		
        tempJavaDocUri = directory.toPath().resolve("src/main/java/org/test/TempClass.java").toUri().toString();
		bean1 = new Bean("ownerRepository", "org.springframework.samples.petclinic.owner.OwnerRepository", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		bean2 = new Bean("ownerService", "org.springframework.samples.petclinic.owner.OwnerService", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		bean3 = new Bean("visitRepository", "org.springframework.samples.petclinic.owner.VisitRepository", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		bean4 = new Bean("visitService", "org.springframework.samples.petclinic.owner.VisitService", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		bean5 = new Bean("petService", "org.springframework.samples.petclinic.pet.Inner.PetService", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		bean6 = new Bean("testBeanCompletionClass", "org.sample.test.TestBeanCompletionClass", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, null, false, "symbolLabel");
		
		springIndex.updateBeans(project.getElementName(), new Bean[] {bean1, bean2, bean3, bean4, bean5, bean6});
	}
	
	@AfterEach
	public void restoreIndexState() {
		this.springIndex.updateBeans(project.getElementName(), indexedBeans);
	}
	
	@Test
	public void testBeanCompletion_firstCompletion() throws Exception {
		assertCompletions(getCompletion("owner<*>"), new String[] {"ownerRepository", "ownerService"}, 0, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {

    private final OwnerRepository ownerRepository;

    TestBeanCompletionClass(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

		public void test() {
ownerRepository<*>
		}
}	    
									""");
	}
	
	@Test
	public void testBeanCompletion_secondCompletion() throws Exception {
		assertCompletions(getCompletion("owner<*>"), new String[] {"ownerRepository", "ownerService"}, 1, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerService;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {

    private final OwnerService ownerService;

    TestBeanCompletionClass(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

		public void test() {
ownerService<*>
		}
}	    
									""");
	}
	
	@Test
	public void noPrefix_secondCompletion() throws Exception {
		assertCompletions(getCompletion("<*>"), new String[] {"ownerRepository", "ownerService", "petService", "visitRepository", "visitService"}, 1, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerService;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {

    private final OwnerService ownerService;

    TestBeanCompletionClass(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

		public void test() {
ownerService<*>
		}
}	    
									""");
	}
	
	@Test
	public void testBeanCompletion_injectInnerClass() throws Exception {
		assertCompletions(getCompletion("<*>"), new String[] {"ownerRepository", "ownerService", "petService", "visitRepository", "visitService"}, 2, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.pet.Inner.PetService;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {

    private final Inner.PetService petService;

    TestBeanCompletionClass(Inner.PetService petService) {
        this.petService = petService;
    }

		public void test() {
petService<*>
		}
}	    
									""");
	}
	
	@Test
	public void testBeanCompletion_multipleClasses() throws Exception {
		String content = """
				package org.sample.test;
				
				import org.springframework.samples.petclinic.owner.OwnerRepository;
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
				    private final OwnerRepository ownerRepository;
				
				    TestBeanCompletionClass(OwnerRepository ownerRepository) {
				        this.ownerRepository = ownerRepository;
				    }
						
						public void test() {
						}
				}
				
				@Controller
				public class TestBeanCompletionSecondClass {
						
						public void test() {
						 owner<*>
						}
				}
				""";
		
		assertCompletions(content, new String[] {"ownerRepository", "ownerService"}, 1, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.OwnerService;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {
    private final OwnerRepository ownerRepository;

    TestBeanCompletionClass(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

		public void test() {
		}
}	  

@Controller
public class TestBeanCompletionSecondClass {

    private final OwnerService ownerService;

    TestBeanCompletionSecondClass(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

		public void test() {
		 ownerService<*>
		}
}
									""");
	}
	
	@Test
	public void noPrefix_multipleClasses() throws Exception {
		String content = """
				package org.sample.test;
				
				import org.springframework.samples.petclinic.owner.OwnerRepository;
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
				    private final OwnerRepository ownerRepository;
				
				    TestBeanCompletionClass(OwnerRepository ownerRepository) {
				        this.ownerRepository = ownerRepository;
				    }
						
						public void test() {
						}
				}
				
				@Controller
				public class TestBeanCompletionSecondClass {
						
						public void test() {
						 <*>
						}
				}
				""";
		
		assertCompletions(content, new String[] {"ownerRepository", "ownerService", "petService", "testBeanCompletionClass", "visitRepository", "visitService"}, 1, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.OwnerService;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionClass {
    private final OwnerRepository ownerRepository;

    TestBeanCompletionClass(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

		public void test() {
		}
}	  

@Controller
public class TestBeanCompletionSecondClass {

    private final OwnerService ownerService;

    TestBeanCompletionSecondClass(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

		public void test() {
		 ownerService<*>
		}
}
									""");
	}
	
	@Test
	public void testBeanCompletion_isNotSpringComponent() throws Exception {
		String content = """
				package org.sample.test;
				
				public class TestBeanCompletionClass {
						
						public void test() {
							owner<*>
						}
				}
				""";
		// No suggestions when it is not a spring component
		assertCompletions(content, new String[] {}, 0, "");
	}
	
	@Test
	public void testBeanCompletion_isOutsideMethod() throws Exception {
		String content = """
				package org.sample.test;
				
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
						owner<*>
				}
				""";
		assertCompletions(content, new String[] {}, 0, "");
	}
	
	@Test
	public void testBeanCompletion_nestedComponent() throws Exception {
		String content = """
package org.sample.test;

import org.springframework.stereotype.Component;

@Component
public class TestBeanCompletionClass {
		@Component
		public class Inner {
		
			public void test() {
				owner<*>
			}
		}
}	
				""";
		
		assertCompletions(content, new String[] {"ownerRepository", "ownerService"}, 0, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Component;

@Component
public class TestBeanCompletionClass {
		@Component
		public class Inner {

            private final OwnerRepository ownerRepository;

            Inner(OwnerRepository ownerRepository) {
                this.ownerRepository = ownerRepository;
            }

			public void test() {
				ownerRepository<*>
			}
		}
}	  
							""");
	}
	
	private String getCompletion(String completionLine) {
		String content = """
				package org.sample.test;
				
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
						
						public void test() {
						 """ +
						completionLine + "\n" +
						"""
						}
				}
				""";
		return content;
	}
	
	@Test
	public void beansPresent() throws Exception {
		String content = """
				package org.sample.test;
				
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionSecondClass {
					private final TestBeanCompletionClass testBeanCompletionClass;
				
				    TestBeanCompletionSecondClass(TestBeanCompletionClass testBeanCompletionClass) {
				        this.testBeanCompletionClass = testBeanCompletionClass;
				    }
						
						public void test() {
							owner<*>
						}
				}				
				
				@Controller
				public class TestBeanCompletionClass {
						
						public void test() {
						}
				}
				""";
		
		assertCompletions(content, new String[] {"ownerRepository", "ownerService"}, 0, 
				"""
package org.sample.test;

import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Controller;

@Controller
public class TestBeanCompletionSecondClass {

    private final OwnerRepository ownerRepository;
	private final TestBeanCompletionClass testBeanCompletionClass;

    TestBeanCompletionSecondClass(TestBeanCompletionClass testBeanCompletionClass, OwnerRepository ownerRepository) {
        this.testBeanCompletionClass = testBeanCompletionClass;
        this.ownerRepository = ownerRepository;
    }

		public void test() {
			ownerRepository<*>
		}
}	  

@Controller
public class TestBeanCompletionClass {

		public void test() {
		}
}
									""");
	}
	
	@Test
	public void beanCompletionWithThis() throws Exception {
		String content = """
				package org.sample.test;
				
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
					public void test() {
						this.owner<*>
					}
				}
				""";
		
		
		assertCompletions(content, new String[] {"ownerRepository", "ownerService"}, 0, 
				"""
				package org.sample.test;
				
				import org.springframework.samples.petclinic.owner.OwnerRepository;
				import org.springframework.stereotype.Controller;
				
				@Controller
				public class TestBeanCompletionClass {
				
				    private final OwnerRepository ownerRepository;
				
				    TestBeanCompletionClass(OwnerRepository ownerRepository) {
				        this.ownerRepository = ownerRepository;
				    }
					public void test() {
						this.ownerRepository<*>
					}
				}
				""");
	}

	
	private void assertCompletions(String completionLine, String[] expectedCompletions, int chosenCompletion, String expectedResult) throws Exception {
		assertCompletions(completionLine, expectedCompletions.length, expectedCompletions, chosenCompletion, expectedResult);
	}
	
	private void assertCompletions(String editorContent, int noOfExcpectedCompletions, String[] expectedCompletions, int chosenCompletion, String expectedResult) throws Exception {		
		Editor editor = harness.newEditor(LanguageId.JAVA, editorContent, tempJavaDocUri);

        List<CompletionItem> completions = editor.getCompletions();
        assertEquals(noOfExcpectedCompletions, completions.size());

        if (expectedCompletions != null) {
	        String[] completionItems = completions.stream()
	        	.map(item -> item.getLabel())
	        	.toArray(size -> new String[size]);
	        
	        assertArrayEquals(expectedCompletions, completionItems);
        }
        
        if (noOfExcpectedCompletions > 0) {
	        editor.apply(completions.get(chosenCompletion));
	        assertEquals(expectedResult, editor.getText());
        }
	}

}
