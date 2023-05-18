/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.*;

import org.springframework.ide.vscode.boot.common.SpringProblemCategories;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public enum Boot2JavaProblemType implements ProblemType {

	JAVA_AUTOWIRED_CONSTRUCTOR(WARNING, "Unnecessary `@Autowired` over the only constructor", "Unnecessary `@Autowired`"),

	JAVA_PUBLIC_BEAN_METHOD(HINT, "Public modifier on `@Bean` method. They no longer have to be public visibility to be usable by Spring.", "public `@Bean` method"),
	
	JAVA_TEST_SPRING_EXTENSION(WARNING, "`@SpringBootTest` and all test slice annotations already applies `@SpringExtension` as of Spring Boot 2.1.0.", "Unnecessary `@SpringExtension`"),
	
	JAVA_CONSTRUCTOR_PARAMETER_INJECTION(IGNORE, "Use constructor parameter injection", "Use constructor parameter injection"),
	
	JAVA_PRECISE_REQUEST_MAPPING(HINT, "Use precise mapping annotation, i.e. '@GetMapping', '@PostMapping', etc.", "Use precise mapping annotation, i.e. '@GetMapping', '@PostMapping', etc."),
	
	JAVA_REPOSITORY(WARNING, "Unnecessary `@Repository`", "Unnecessary `@Repository`"),
	
	JAVA_LAMBDA_DSL(INFO, "Consider switching to Lambda DSL syntax", "Switch to Lambda DSL syntax"), 
	
	MISSING_CONFIGURATION_ANNOTATION(WARNING, "Class likely missing '@Configuration' annotation, i.e. has Bean methods but no '@Configuration' annotation", "Missing '@Configuration'"),
	
	HTTP_SECIRITY_AUTHORIZE_HTTP_REQUESTS(WARNING, "'HttpSecurity.authroizeRequests(...)' API and related classes are to be deprecated use new `authorizeHttpRequests(...) and related classes", "Usage of old 'HttpSecurity.authroizeRequests(...)' API"),
	
	WEB_SECURITY_CONFIGURER_ADAPTER(WARNING, "'WebSecurityConfigurerAdapter' is removed in Spring-Security 6.x. Refactor classes extending the 'WebSecurityConfigurerAdapter' into 'Configuration' beans and methods into 'Bean' definitions ", "Replace usage of 'WebSecurityConfigurerAdapter' as this class to be removed in Security 6.x"),
	
	DOMAIN_ID_FOR_REPOSITORY(ERROR, "Invalid Domain ID type for Spring Data Repository", "Invalid Domain ID Type for Spring Data Repository");
	
	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private Boot2JavaProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	private Boot2JavaProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
	}

	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public String getLabel() {
		if (label==null) {
			label = createDefaultLabel();
		}
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	private String createDefaultLabel() {
		String label = this.toString().substring(5).toLowerCase().replace('_', ' ');
		return Character.toUpperCase(label.charAt(0)) + label.substring(1);
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public ProblemCategory getCategory() {
		return SpringProblemCategories.BOOT_2;
	}

}
