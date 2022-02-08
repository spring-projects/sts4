/*******************************************************************************
 * Copyright (c) 2018, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.JavaType.Method;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * @author Martin Lippert
 */
public class DomainType {

	private final String packageName;
	private final String fullName;
	private final String simpleName;
	private Supplier<DomainProperty[]> properties;

	public DomainType(String packageName, String fullName, String simpleName) {
		this.packageName = packageName;
		this.fullName = fullName;
		this.simpleName = simpleName;
	}

	public DomainType(FullyQualified type) {
		this.packageName = type.getPackageName();
		this.fullName = type.getFullyQualifiedName();
		this.simpleName = type.getClassName();

		this.properties = Suppliers.memoize(() -> {
			if (!this.packageName.startsWith("java")) {
				List<Method> methods = type.getMethods();
				if (methods != null && !methods.isEmpty()) {
					List<DomainProperty> properties = new ArrayList<>();

					for (Method method : methods) {
						String methodName = method.getName();
						if (methodName != null && methodName.startsWith("get")) {
							String propertyName = methodName.substring(3);
							JavaType returnType = method.getReturnType();
							if (returnType instanceof FullyQualified) {
								properties.add(new DomainProperty(propertyName, new DomainType((FullyQualified) returnType)));
							}
						}
					}
					return (DomainProperty[]) properties.toArray(new DomainProperty[properties.size()]);
				}
			}
			return new DomainProperty[0];
		});
	}

	public String getPackageName() {
		return packageName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public DomainProperty[] getProperties() {
		return properties.get();
	}

}
