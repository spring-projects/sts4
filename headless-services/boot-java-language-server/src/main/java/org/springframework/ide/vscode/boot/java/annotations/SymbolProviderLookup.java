///*******************************************************************************
// * Copyright (c) 2017 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.vscode.boot.java.annotations;
//
//import java.util.Collection;
//
//import org.eclipse.jdt.core.dom.ITypeBinding;
//import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
//
//import com.google.common.collect.ImmutableList;
//
//import reactor.util.function.Tuple2;
//
///**
// * @author Kris De Volder
// */
//public class SymbolProviderLookup {
//
//	@FunctionalInterface
//	public interface Factory<T> {
//		T create(String fqAnnotationType);
//	}
//
//	private AnnotationHierarchyAwareLookup<SymbolProvider> providers = new AnnotationHierarchyAwareLookup<>();
//
//	public void put(String fqAnnotationType, SymbolProvider value) {
//		providers.put(fqAnnotationType, true, value);
//	}
//
//	public Collection<SymbolProvider> get(ITypeBinding typeBinding) {
//		ImmutableList.Builder<T> builder = ImmutableList.builder();
//		for (Tuple2<String, Factory<T>> entry : factories.get(typeBinding)) {
//			String superAnnotationName = entry.getT1();
//			Factory<T> factory = entry.getT2();
//			T element = factory.create(superAnnotationName);
//			if (element!=null) {
//				builder.add(element);
//			}
//		}
//		return builder.build();
//	}
//
//}
