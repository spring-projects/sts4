package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.ImplicitWebAnnotationNamesReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class ImplicitWebAnnotationNamesReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "implicitwebannotationnames";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new ImplicitWebAnnotationNamesReconciler(new QuickfixRegistry());
	}
	
	@BeforeEach
	void setup() throws Exception {
		super.setup();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Test
	void sanityTest() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@PathVariable("message") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@PathVariable(\"message\")", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		assertEquals("Remove Implicit Web Annotation Name", problems.get(0).getQuickfixes().get(0).title);
		assertEquals("Remove Implicit Web Annotation Names in file", problems.get(0).getQuickfixes().get(1).title);
		assertEquals("Remove Implicit Web Annotation Names in project", problems.get(0).getQuickfixes().get(2).title);
		
	}
	
	@Test
	void webAnnotationWithDifferentNames() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{msg}")
					public String hello(@PathVariable("msg") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}
	
	@Test
	void webAnnotationWithAssignment() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@PathVariable(value="message") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@PathVariable(value=\"message\")", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		
	}
	
	@Test
	void webAnnotationWithMultipleParams() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.RequestParam;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@RequestParam(name="message", defaultValue = "world") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@RequestParam(name=\"message\", defaultValue = \"world\")", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		
	}
	
	@Test
	void multipleWebAnnotations() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}/{name}/{address}")
					public String hello(@PathVariable(value="message", required=false) String message, @PathVariable(value="name") String name, @PathVariable(value="address", required=false) String location) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(2, problems.size());
		
		ReconcileProblem problem1 = problems.get(0);
		ReconcileProblem problem2 = problems.get(1);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem1.getType());
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem2.getType());
		
		String markedStr1 = source.substring(problem1.getOffset(), problem1.getOffset() + problem1.getLength());
		assertEquals("@PathVariable(value=\"message\", required=false)", markedStr1);
		
		String markedStr2 = source.substring(problem2.getOffset(), problem2.getOffset() + problem2.getLength());
		assertEquals("@PathVariable(value=\"name\")", markedStr2);

		assertEquals(3, problem1.getQuickfixes().size());
		assertEquals(3, problem2.getQuickfixes().size());
		
	}

}
