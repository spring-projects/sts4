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
package org.springframework.ide.vscode.boot.java.data;

import java.util.List;

/**
 * Represents information about the subject of a JPA query method.
 *
 * See https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#appendix.query.method.subject
 * @author danthe1st
 */
record QueryMethodSubject(
		String key, String returnType, boolean isTyped) {

	static final List<QueryMethodSubject> QUERY_METHOD_SUBJECTS = List.of(
			QueryMethodSubject.createCollectionSubject("find", "List"),
			QueryMethodSubject.createCollectionSubject("read", "List"),
			QueryMethodSubject.createCollectionSubject("get", "List"),
			QueryMethodSubject.createCollectionSubject("query", "List"),
			QueryMethodSubject.createCollectionSubject("search", "List"),
			QueryMethodSubject.createCollectionSubject("stream", "Streamable"),
			QueryMethodSubject.createPrimitiveSubject("exists", "boolean"),
			QueryMethodSubject.createPrimitiveSubject("count", "long"),
			QueryMethodSubject.createPrimitiveSubject("delete", "void"),
			QueryMethodSubject.createPrimitiveSubject("remove", "void")
			);

	private static QueryMethodSubject createPrimitiveSubject(String key, String primitive) {
		return new QueryMethodSubject(key, primitive, false);
	}
	private static QueryMethodSubject createCollectionSubject(String key, String collectionType) {
		return new QueryMethodSubject(key, collectionType, true);
	}

}