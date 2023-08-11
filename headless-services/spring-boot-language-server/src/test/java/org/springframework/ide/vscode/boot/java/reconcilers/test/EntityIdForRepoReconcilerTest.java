/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.EntityIdForRepoReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class EntityIdForRepoReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "repoentityid";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new EntityIdForRepoReconciler();
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
	void invalidDomainId_string_number() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Number.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void noId() throws Exception {
		Path employeeSource = createFile("Employee.java", """
					package demo;

					public class Employee {
						String idNotForMongo;
					}
				""");

		String source = """
					package demo;

					import org.springframework.data.repository.RepositoryDefinition;

					@RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
					interface EmployeeRepository {}
				""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);

		assertEquals(0, problems.size());

	}
	
	@Test
	void noRepoBean() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.NoRepositoryBean;
			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
			@NoRepositoryBean
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(0, problems.size());
	}
	
	@Test
	void invalidDomainId_string_long() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Long.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void invalidDomainId_integer_double() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id Integer id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Double.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Double.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.Integer'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void validDomainId_CharSequence_String() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = CharSequence.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(0, problems.size());
		
	}

	@Test
	void validDomainId_String_CharSequence() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id CharSequence id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = String.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(0, problems.size());
		
	}

	@Test
	void validDomainId_int_Integer() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Employee {
				@Id int id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Integer.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(0, problems.size());
		
	}
	
	@Test
	void inheritance_invalidDomainId_string_number_1() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface CustomerRepository extends Repository<Customer, Long> {}
		""";
		List<ReconcileProblem> problems = reconcile("CustomerRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_invalidDomainId_string_number_2() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface CustomerRepository<T extends Customer, ID extends Long> extends Repository<T, ID> {}
		""";
		List<ReconcileProblem> problems = reconcile("CustomerRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("ID extends Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_intermediateRepo_1() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyIntermediateRepository.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyIntermediateRepository<T extends Customer, ID extends Number> extends Repository<T, ID>{}
            """); 
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyConcreteRepository extends MyIntermediateRepository<Customer, Long>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyConcreteRepository.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_intermediateRepo_2() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository1.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository1<T extends Customer> extends Repository<T, Long>{}
            """); 
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository1 extends MyOtherIntermediateRepository1<Customer>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository1.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("MyOtherIntermediateRepository1<Customer>", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void inheritance_intermediateRepo_3() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository2.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository2<ID extends Number> extends Repository<Customer, ID>{}
            """); 
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository2 extends MyOtherIntermediateRepository2<Long>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository2.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_extra_interface() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository2.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository2<ID extends Number, T> extends Repository<Customer, ID>, Iterator<T>{}
            """); 
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository2 extends MyOtherIntermediateRepository2<Long, String>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository2.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_reversed_template_params_1() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository1.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository1<ID extends Number, T> extends Repository<T, ID>{}
            """); 
		
		Path interRepo2 = createFile("MyOtherIntermediateRepository2.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository2<ID, T> extends MyOtherIntermediateRepository1<ID, T>{}
			""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository2 extends MyOtherIntermediateRepository2<Long, Customer>
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository2.java", source, false, interRepo, interRepo2, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void inheritance_reversed_template_params_2() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository1.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository1<ID, DOMAIN> extends Repository<DOMAIN, ID>{}
            """); 
		
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository2 extends MyOtherIntermediateRepository1<Long, Customer>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository2.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Long", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_advanced_1() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository1.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            import java.util.List;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository2<S, T, ID, D> extends Repository<D, ID>, Iterator<T>, List<S>{}
            """); 
		
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherConcreteRepository2 extends MyOtherIntermediateRepository2<Long, String, Long, Customer>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherConcreteRepository2.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("MyOtherConcreteRepository2", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void inheritance_advanced_2() throws Exception {
		
		Path employeeSource = createFile("Customer.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public class Customer {
				@Id String id;
			}
		""");
		
		Path interRepo = createFile("MyOtherIntermediateRepository1.java", """
            package demo;
            
            import org.springframework.data.repository.NoRepositoryBean;
            import org.springframework.data.repository.Repository;
            import java.util.List;
            
            @NoRepositoryBean
            interface MyOtherIntermediateRepository1<S, T, ID, D> extends Repository<D, ID>, Iterator<T>, List<S>{}
            """); 
		
		
		String source = """
			package demo;

			import org.springframework.data.repository.Repository;

			interface MyOtherIntermediateRepository2<S> extends MyOtherIntermediateRepository1<S, String, Long, Customer>{}
		""";
		
		List<ReconcileProblem> problems = reconcile("MyOtherIntermediateRepository2.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("MyOtherIntermediateRepository2", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void invalid_DomainId_record() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public record Employee(@Id String id) {};
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Number.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void valid_DomainId_record() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
			package demo;

			import org.springframework.data.annotation.Id;

			public record Employee(@Id String id) {};
		""");
		
		String source = """
			package demo;

			import org.springframework.data.repository.RepositoryDefinition;

			@RepositoryDefinition(domainClass = Employee.class, idClass = String.class)
			interface EmployeeRepository {}
		""";
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(0, problems.size());
		
	}
	
	@Test
	void domain_type_with_inheritance_1() throws Exception {
		
		Path employeeSource = createFile("Person.java", """
	            package demo;
	            
	            import org.springframework.data.annotation.Id;
	            
	            public class Person {
	                @Id String id;
	            }
	            """);
		
		Path interRepo = createFile("Employee.java", """
	            package demo;
	            
	            public class Employee extends Person {
	            }
	            """); 
		
		
		String source = """
	              package demo;

	              import org.springframework.data.repository.RepositoryDefinition;
	              
	              @RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
	              interface EmployeeRepository {}
	              """;
		
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, interRepo, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Number.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void domain_type_with_inheritance_2() throws Exception {
		
		Path employeeSource = createFile("Person.java", """
	            package demo;
	            
	            import org.springframework.data.annotation.Id;
	            
	            public class Person {
	                @Id String idNotForMongo;
	            }
	            """);
		
		Path interRepo = createFile("Employee.java", """
	            package demo;
	            
	            public class Employee extends Person {
	                String idNotForMongo;
	            }
	            """); 
		
		
		String source = """
	              package demo;

	              import org.springframework.data.repository.RepositoryDefinition;
	              
	              @RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
	              interface EmployeeRepository {}
	              """;
		
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, interRepo, employeeSource);
		
		assertEquals(0, problems.size());
		
	}
	@Test
	void id_field_invalid_DomainId_1() throws Exception {
		
		Path employeeSource = createFile("Employee.java", """
	            package demo;
	            
	            public class Employee {
	                String id;
	            }
	            """);
		
		String source = """
	              package demo;

	              import org.springframework.data.repository.RepositoryDefinition;
	              
	              @RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
	              interface EmployeeRepository {}
	              """;
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Number.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void id_field_invalid_DomainId_2() throws Exception {
		
		Path personSource = createFile("Person.java", """
	            package demo;
	            
	            public class Person {
	                String id;
	            }
	            """);
		
		Path employeeSource = createFile("Employee.java", """
	            package demo;
	            
	            public class Employee extends Person {
	            }
	            """);
		
		String source = """
	              package demo;

	              import org.springframework.data.repository.RepositoryDefinition;
	              
	              @RepositoryDefinition(domainClass = Employee.class, idClass = Number.class)
	              interface EmployeeRepository {}
	              """;
		List<ReconcileProblem> problems = reconcile("EmployeeRepository.java", source, false, personSource, employeeSource);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("Number.class", markedStr);
		assertEquals("Expected Domain ID type is 'java.lang.String'", problem.getMessage());

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
}
