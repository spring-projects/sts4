/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java.parser;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.util.Log;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public interface CompilationUnitIndex {
	
	static final CompilationUnitIndex DEFAULT = new CompilationUnitIndex() {
		
		private LoadingCache<URL, CompilationUnit> cache = CacheBuilder.newBuilder().build(new CacheLoader<URL, CompilationUnit>() {

			@Override
			public CompilationUnit load(URL url) throws Exception {
				return JavaParser.parse(url.openStream());
			}
			
		});

		@Override
		public CompilationUnit getCompilationUnit(URL url) {
			try {
				return cache.get(url);
			} catch (ExecutionException e) {
				Log.log(e);
			}
			return null;
		}

	};
	
	CompilationUnit getCompilationUnit(URL url); 

}
