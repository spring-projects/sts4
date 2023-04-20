/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * @author Martin Lippert
 */
public class DomainType extends SimpleType {

	private Supplier<DomainProperty[]> properties;
	private Set<String> usedTypes;

	public DomainType(ITypeBinding typeBinding) {
		super(typeBinding);

		this.properties = Suppliers.memoize(() -> {
			List<DomainProperty> domainProps = calculateDomainProperties(typeBinding);
			return domainProps.toArray(new DomainProperty[domainProps.size()]);
		});

		this.usedTypes = new HashSet<>();
		fillImports(typeBinding);
	}
	
	private void fillImports(ITypeBinding binding) {
		if (binding != binding.getErasure()) {
			fillImports(binding.getErasure());
			for (ITypeBinding ta : binding.getTypeArguments()) {
				fillImports(ta);
			}
		} else if (binding.isClass() || binding.isInterface() || binding.isEnum() || binding.isRecord()) {
			String fqName = binding.getQualifiedName();
			usedTypes.add(fqName);
		}
	}
	
	private List<DomainProperty> calculateDomainProperties(ITypeBinding typeBinding) {
		if (!getPackageName().startsWith("java")) {
			IMethodBinding[] methods = typeBinding.getDeclaredMethods();
			if (methods != null && methods.length > 0) {
				List<DomainProperty> properties = new ArrayList<>();

				for (IMethodBinding method : methods) {
					String methodName = method.getName();
					if (methodName != null) {
						String propertyName = null;
						if (methodName.startsWith("get")) {
							propertyName = methodName.substring(3);
						}
						else if (methodName.startsWith("is")) {
							propertyName = methodName.substring(2);
						}
						if (propertyName != null) {
							properties.add(new DomainProperty(propertyName, new DomainType(method.getReturnType())));
						}
					}
				}
				
				if (typeBinding.getSuperclass() != null) {
					properties.addAll(calculateDomainProperties(typeBinding.getSuperclass()));
				}
				
				if (typeBinding.getInterfaces() != null) {
					for (ITypeBinding si : typeBinding.getInterfaces()) {
						properties.addAll(calculateDomainProperties(si));
					}
				}
				
				return properties;
			}
		}
		return Collections.emptyList();
	}

	public DomainProperty[] getProperties() {
		return properties.get();
	}
	
	public Map<String, DomainProperty> getPropertiesByName() {
		Map<String, DomainProperty> propertiesByName = new LinkedHashMap<>();
		for(DomainProperty prop : properties.get()){
			propertiesByName.put(prop.getName(), prop);
		}
		return propertiesByName;
	}
	
	public Set<String> getUsedTypes() {
		return usedTypes;
	}

}
