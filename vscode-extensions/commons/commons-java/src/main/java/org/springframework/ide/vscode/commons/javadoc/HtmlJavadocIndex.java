package org.springframework.ide.vscode.commons.javadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.javadoc.internal.JavadocContents;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public interface HtmlJavadocIndex {
	
	static JavadocContents NO_HTML_CONTENT = new JavadocContents(null);
	
	public static final HtmlJavadocIndex DEFAULT = new HtmlJavadocIndex() {
		
		private Cache<URL, JavadocContents> cache = CacheBuilder.newBuilder().build();
		

		@Override
		public JavadocContents getHtmlJavadoc(URL url) {
			try {
				JavadocContents content = cache.get(url, () -> {
					InputStream stream = null;
					try {
						stream = url.openStream();
						BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
					    return new JavadocContents(buffer.lines().collect(Collectors.joining("\n")));
					} catch (IOException e) {
						Log.log(e);
						return NO_HTML_CONTENT;
					} finally {
						if (stream != null) {
							stream.close();
						}
					}
				});
				return content == NO_HTML_CONTENT ? null : content;
			} catch (ExecutionException e) {
				Log.log(e);
				return null;
			}
		}
	};
	
	JavadocContents getHtmlJavadoc(URL url);

}
