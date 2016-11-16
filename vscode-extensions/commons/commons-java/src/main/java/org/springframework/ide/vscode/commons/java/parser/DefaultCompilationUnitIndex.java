package org.springframework.ide.vscode.commons.java.parser;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.util.Log;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class DefaultCompilationUnitIndex implements CompilationUnitIndex {
	
	private LoadingCache<URL, CompilationUnit> cache = CacheBuilder.newBuilder().build(new CacheLoader<URL, CompilationUnit>() {

		@Override
		public CompilationUnit load(URL url) throws Exception {
			InputStream in = url.openStream();
			try {
				return JavaParser.parse(in);
			} catch (ParseException e) {
				in.close();
				Log.log("Failed to parse java source file: " + url, e);
			}
			return null;
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

}
