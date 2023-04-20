/*******************************************************************************
 * Copyright (c) 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.providers;

import java.util.List;

/**
 * Represents information about the subject of a JPA query method.
 *
 * See https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#appendix.query.method.subject
 * @author danthe1st
 */
public record QueryMethodSubject(
		String key, String returnType, String fqName) {
	
	private static final String LIST = "List";
	private static final String LIST_FQ = "java.util.List";
	
	public static final List<QueryMethodSubject> QUERY_METHOD_SUBJECTS = List.of(
			QueryMethodSubject.createCollectionSubject("find", LIST, LIST_FQ),
			QueryMethodSubject.createCollectionSubject("read", LIST, LIST_FQ),
			QueryMethodSubject.createCollectionSubject("get", LIST, LIST_FQ),
			QueryMethodSubject.createCollectionSubject("query", LIST, LIST_FQ),
			QueryMethodSubject.createCollectionSubject("search", LIST, LIST_FQ),
			QueryMethodSubject.createCollectionSubject("stream", "Streamable", "org.springframework.data.util.Streamable"),
			QueryMethodSubject.createPrimitiveSubject("exists", "boolean"),
			QueryMethodSubject.createPrimitiveSubject("count", "long"),
			QueryMethodSubject.createPrimitiveSubject("delete", "void"),
			QueryMethodSubject.createPrimitiveSubject("remove", "void")
			);

	private static QueryMethodSubject createPrimitiveSubject(String key, String primitive) {
		return new QueryMethodSubject(key, primitive, null);
	}
	private static QueryMethodSubject createCollectionSubject(String key, String collectionTypeSimpleName, String collectionTypeFqName) {
		return new QueryMethodSubject(key, collectionTypeSimpleName, collectionTypeFqName);
	}

}