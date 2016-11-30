package org.springframework.ide.vscode.application.properties.metadata;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.application.properties.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.vscode.application.properties.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.commons.java.Flags;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.publisher.Flux;

public class ClassReferenceProvider extends CachingValueProvider {
	
	/**
	 * Default value for the 'concrete' parameter.
	 */
	private static final boolean DEFAULT_CONCRETE = true;

	private static final ClassReferenceProvider UNTARGETTED_INSTANCE = new ClassReferenceProvider(null, DEFAULT_CONCRETE);

	public static final Function<Map<String, Object>, ValueProviderStrategy> FACTORY = applyOn(
		1, TimeUnit.MINUTES,
		(params) -> {
			String target = getTarget(params);
			Boolean concrete = getConcrete(params);
			if (target!=null || concrete!=null) {
				if (concrete==null) {
					concrete = DEFAULT_CONCRETE;
				}
				return new ClassReferenceProvider(target, concrete);
			}
			return UNTARGETTED_INSTANCE;
		}
	);
	
	public static <K,V> Function<K,V> applyOn(long duration, TimeUnit unit, Function<K,V> func) {
		Cache<K,V> cache = CacheBuilder.newBuilder().expireAfterAccess(duration, unit).expireAfterWrite(duration, unit).build();
		return (k) -> {
			try {
				return cache.get(k, () -> func.apply(k));
			} catch (ExecutionException e) {
				Log.log(e);
				return null;
			}
		};
	}

	private static String getTarget(Map<String, Object> params) {
		if (params!=null) {
			Object obj = params.get("target");
			if (obj instanceof String) {
				String target = (String) obj;
				if (StringUtil.hasText(target)) {
					return target;
				}
			}
		}
		return null;
	}

	private static boolean isAbstract(IType type) {
		try {
			return type.isInterface() || Flags.isAbstract(type.getFlags());
		} catch (Exception e) {
			Log.log(e);
			return false;
		}
	}

	private static Boolean getConcrete(Map<String, Object> params) {
		try {
			if (params!=null) {
				Object obj = params.get("concrete");
				if (obj instanceof String) {
					String concrete = (String) obj;
					return Boolean.valueOf(concrete);
				} else if (obj instanceof Boolean) {
					return (Boolean) obj;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	/**
	 * Optional, fully qualified name of the 'target' type. Suggested hints should be a subtype of this type.
	 */
	private String target;

	/**
	 * Optional parameter, whether only concrete types should be suggested. Default value is true.
	 */
	private boolean concrete;

	private ClassReferenceProvider(String target, boolean concrete) {
		this.target = target;
		this.concrete = concrete;
	}

	@Override
	protected Flux<StsValueHint> getValuesAsync(IJavaProject javaProject, String query) {
		IType targetType = target == null || target.isEmpty() ? javaProject.findType("java.lang.Object") : javaProject.findType(target);
		if (targetType == null) {
			return Flux.empty();
		}
		Set<IType> allSubclasses = javaProject
				.allSubtypesOf(targetType)
				.filter(t -> Flags.isPublic(t.getFlags()) && !concrete || !isAbstract(t))
				.collect(Collectors.toSet())
				.block();
		if (allSubclasses.isEmpty()) {
			return Flux.empty();
		} else {
			return javaProject
					.fuzzySearchTypes(query, type -> allSubclasses.contains(type))
					.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
					.flatMap(l -> Flux.fromIterable(l))
					.map(t -> StsValueHint.create(t.getT1()));
		}
	}

}
