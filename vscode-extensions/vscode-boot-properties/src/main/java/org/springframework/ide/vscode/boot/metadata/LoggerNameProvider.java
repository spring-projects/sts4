package org.springframework.ide.vscode.boot.metadata;

import java.util.Map;
import java.util.function.Function;

import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.vscode.boot.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

/**
 * Provides the algorithm for 'logger-name' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 * @author Alex Boyko
 */
public class LoggerNameProvider extends CachingValueProvider {
	
	private  static final ValueProviderStrategy INSTANCE = new LoggerNameProvider();
	public static final Function<Map<String, Object>, ValueProviderStrategy> FACTORY = (params) -> INSTANCE;

	@Override
	protected Flux<StsValueHint> getValuesAsync(IJavaProject javaProject, String query) {
		return Flux.concat(
			javaProject
				.fuzzySearchPackages(query)
				.map(t -> Tuples.of(StsValueHint.create(t.getT1()), t.getT2())),
			javaProject
				.fuzzySearchTypes(query, null)
				.map(t -> Tuples.of(StsValueHint.create(t.getT1()), t.getT2()))
			)
		.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
		.flatMap(l -> Flux.fromIterable(l))
		.map(t -> t.getT1());
	}

}
