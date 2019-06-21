/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.MethodDelegation.WithCustomProperties;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher.Junction;

/**
 * Utility to instrument a given class, memoizing all it's zero-argument method invocations. 
 * Note that the memoization behaves a little different from guava's Suppliers.memoize in that
 * it caches exception results as well as regularly returned values.
 */
public class MemoizingProxy {

	public interface Builder<T> {
		T build(Object... args);
	}
	
	private static final Junction<MethodDescription> CACHABLE_METHODS = 
			takesArguments(0).and(not(isStatic()).and(isPublic()));

	public interface IFieldProxy {
		Object getValue();
		void setValue(Object value);
	}
	
	private static final String F_CACHE = "__MemoizingProxy__cache";
	private static final String F_DURATION = "__MemoizingProxy__duration";
	
	private static final MethodDelegation.WithCustomProperties METHOD_DELEGATION = MethodDelegation.withDefaultConfiguration()
			.withBinders(FieldProxy.Binder.install(IFieldProxy.class));

	public static class ConstructorInterceptor {
		public static void intercept(@FieldProxy(F_CACHE) IFieldProxy fCache, @FieldValue(F_DURATION) long duration) {
			fCache.setValue(CacheBuilder.newBuilder().expireAfterWrite(duration, TimeUnit.MILLISECONDS).build());
		}
	}
	
	public static class MethodInterceptor {
		@RuntimeType
		public static Object intercept(
				@Origin(cache = true) Method method,
				@FieldValue(F_CACHE) Cache<String,Result> cache, 
				@SuperCall Callable<?> zuper, 
				@AllArguments Object[] args
		) throws Exception {
			synchronized (cache) {
				String mname = method.getName();
				Result r = cache.get(mname, () -> new Result(() -> { 
					try {
						return zuper.call();
					} catch (Throwable e) {
						throw ExceptionUtil.exception(e);
					}
				}));
				return r.get();
			}
		}
	}

	static class Result {
		
		Throwable e;
		Object v;
		
		Result(Callable<?> computer) {
			try {
				v = computer.call();
			} catch (Throwable e) {
				this.e = e;
			}
		}

		public Object get() throws Exception {
			if (e!=null) {
				throw ExceptionUtil.exception(e);
			}
			return v;
		}
	}

	public static <T> Builder<T> builder(Class<T> klass, Duration duration, Class<?>... argTypes) throws Exception {
		DynamicType.Builder<T> builder = new ByteBuddy()
				.subclass(klass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
				.defineField(F_DURATION, long.class, Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC).value(duration.toMillis())
				.defineField(F_CACHE, Cache.class, Opcodes.ACC_PRIVATE)
				.method(CACHABLE_METHODS).intercept(MethodDelegation.to(MethodInterceptor.class))
				.defineConstructor(Visibility.PUBLIC).withParameters(argTypes).intercept(
						MethodCall.invoke(klass.getConstructor(argTypes)).withAllArguments()
						.andThen(METHOD_DELEGATION.to(ConstructorInterceptor.class))
				);
		
		Constructor<? extends T> constructor = builder.make().load(klass.getClassLoader()).getLoaded().getConstructor(argTypes);
		return new Builder<T>() {
			@Override
			public T build(Object... args) {
				try {
					return constructor.newInstance(args);
				} catch (Exception e) {
					throw ExceptionUtil.unchecked(e);
				}
			}
		};
	}
	
	/**
	 * Deprecated: use the 'builder' method instead
	 */
	@Deprecated
	public static <T> T create(Class<T> klass, Duration duration, Class<?>[] argTypes, Object... args) {
		try {
			return builder(klass, duration, argTypes).build(args);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
		
//		Enhancer enhancer = new Enhancer();
//		enhancer.setSuperclass(klass);
//		enhancer.setCallback(new MethodInterceptor() {
//			Cache<String, Result> cache = CacheBuilder.newBuilder()
//					.expireAfterWrite(duration.toMillis(), TimeUnit.MILLISECONDS)
//					.build();
//			
//			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//				if (Modifier.isPublic(method.getModifiers()) && (args==null || args.length==0)) {
//					synchronized (cache) {
//						String mname = method.getName();
//						Result r = cache.get(mname, () -> new Result(() -> { 
//							try {
//								return proxy.invokeSuper(obj, args);
//							} catch (Throwable e) {
//								throw ExceptionUtil.exception(e);
//							}	
//						}));
//						return r.get();
//					}
//				} else {
//					return proxy.invokeSuper(obj, args);
//				}
//			}
//		});
//
//		return (T) enhancer.create(argTypes, args);
//	}
	

//	public static class MemoizingProxyHandler implements MethodInterceptor {
//		
//		private final Object original;
//		private final Cache<String, Result> cache;
//		
//		public MemoizingProxyHandler(Object original, Duration cacheExpiresAfter) {
//			this.original = original;
//			this.cache = CacheBuilder.newBuilder()
//					.expireAfterWrite(cacheExpiresAfter.toMillis(), TimeUnit.MILLISECONDS)
//					.build();
//		}
//
//		@Override
//		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//			if (Modifier.isPublic(method.getModifiers()) && (args == null || args.length < 2)) {
//				synchronized (cache) {
//					String mname = method.getName();
//					
//					if (args != null && args.length == 1) {
//						mname += "-" + args[0].toString();
//					}
//					
//					Result r = cache.get(mname, () -> new Result(() -> { 
//						try {
//							return method.invoke(original, args);
//						} catch (Throwable e) {
//							throw ExceptionUtil.exception(e);
//						}	
//					}));
//					return r.get();
//				}
//			} else {
//				return method.invoke(original, args);
//			}
//		}
//		
//	}
	
}
