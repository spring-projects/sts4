package org.springframework.ide.vscode.commons.java.roaster;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaUnit;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class DefaultJavaUnitIndex implements JavaUnitIndex {

	private LoadingCache<URL, JavaUnit> cache = CacheBuilder.newBuilder().build(new CacheLoader<URL, JavaUnit>() {

		@Override
		public JavaUnit load(URL url) throws Exception {
			InputStream in = url.openStream();
			try {
				return Roaster.parseUnit(in);
			} finally {
				in.close();
			}
		}
		
	});

	@Override
	public JavaUnit getJavaUnit(URL url) {
		try {
			return cache.get(url);
		} catch (ExecutionException e) {
			Log.log(e);
		}
		return null;
	}

}
