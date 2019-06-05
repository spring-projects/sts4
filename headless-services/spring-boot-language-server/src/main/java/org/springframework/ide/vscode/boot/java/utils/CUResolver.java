/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Reflection based implementation of JDT package public CompilationUnitResolver.
 * It is used to resolve the {@link CompilationUnitDeclaration}
 * 
 * @author Alex Boyko
 *
 */
class CUResolver {
	
	private static final Logger log = LoggerFactory.getLogger(CUResolver.class);
	
	private static final Supplier<Class<?>> BINDING_TABLES_CLASS = () -> {
		try {
			return Class.forName("org.eclipse.jdt.core.dom.DefaultBindingResolver$BindingTables");
		} catch (ClassNotFoundException e) {
			log.error("{}", e);
			return null;
		}
	};
	
	private static final Supplier<Constructor<?>> BINDING_TABLES_CONSTRUCTOR = () -> {
		try {
			Class<?> clazz = BINDING_TABLES_CLASS.get();
			if (clazz != null) {
				Constructor<?> ctor = clazz.getDeclaredConstructor();
				ctor.setAccessible(true);
				return ctor;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Method> GET_CLASSPATH_METHOD = () -> {
		try {
			Method getClasspathMethod = ASTParser.class.getDeclaredMethod("getClasspath");
			getClasspathMethod.setAccessible(true);
			return getClasspathMethod;
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
			return null;
		}
	};

	private static final Supplier<Class<?>> COMPILATION_UNIT_RESOLVER_CLASS = () -> {
		try {
			return Class.forName("org.eclipse.jdt.core.dom.CompilationUnitResolver");
		} catch (ClassNotFoundException e) {
			log.error("{}", e);
			return null;
		}

	};
	
	private static final Supplier<Constructor<?>> COMPILATION_UNIT_RESOLVER_CONSTRUCTOR = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Constructor<?> ctor = clazz.getDeclaredConstructor(INameEnvironment.class, IErrorHandlingPolicy.class,
						CompilerOptions.class, ICompilerRequestor.class, IProblemFactory.class, IProgressMonitor.class,
						boolean.class);
				ctor.setAccessible(true);
				return ctor;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Constructor<?>> LOOKUP_ENVIRONMENT_CONSTRUCTOR = () -> {
		Class<?> nameEnvironmentWithProgressClass;
		try {
			nameEnvironmentWithProgressClass = Class.forName("org.eclipse.jdt.core.dom.NameEnvironmentWithProgress");
			Constructor<?> lookupCtor = nameEnvironmentWithProgressClass.getDeclaredConstructor(
					Classpath[].class,
					String[].class,
					IProgressMonitor.class
			);
			lookupCtor.setAccessible(true);
			return lookupCtor;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
			return null;
		}
	};
	
	private static final Supplier<Method> GET_HANDLER_POLICY_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Method handlerPolicyMethod = clazz.getDeclaredMethod("getHandlingPolicy");
				handlerPolicyMethod.setAccessible(true);
				return handlerPolicyMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};

	private static final Supplier<Method> GET_REQUESTOR_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Method getRequestorMethod = clazz.getDeclaredMethod("getRequestor");
				getRequestorMethod.setAccessible(true);
				return getRequestorMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};

	private static final Supplier<Method> GET_COMPILER_OPTIONS_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Method compilerOptionsMethod = clazz.getDeclaredMethod("getCompilerOptions", Map.class, boolean.class);
				compilerOptionsMethod.setAccessible(true);
				return compilerOptionsMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Class<?>> NODE_SEARCHER_CLASS = () -> {
		try {
			return Class.forName("org.eclipse.jdt.core.dom.NodeSearcher");
		} catch (ClassNotFoundException e) {
			log.error("{}", e);
			return null;
		}
	};
	
	private static final Supplier<Method> PARSE_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			Class<?> nodeSearcherClass = NODE_SEARCHER_CLASS.get();
			if (clazz != null && nodeSearcherClass != null) {
				Method parseMethod = clazz.getDeclaredMethod("parse", 
						org.eclipse.jdt.internal.compiler.env.ICompilationUnit.class,
						nodeSearcherClass,
						Map.class,
						int.class);
				parseMethod.setAccessible(true);
				return parseMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Method> RESOLVE_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			Class<?> nodeSearcherClass = NODE_SEARCHER_CLASS.get();
			if (clazz != null && nodeSearcherClass != null) {
				Method resolveMethod = clazz.getDeclaredMethod("resolve", 
						CompilationUnitDeclaration.class,
						org.eclipse.jdt.internal.compiler.env.ICompilationUnit.class,
						nodeSearcherClass,
						boolean.class,
						boolean.class,
						boolean.class);
				resolveMethod.setAccessible(true);
				return resolveMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Method> CONVERT_METHOD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Method convertMethod = clazz.getDeclaredMethod("convert", 
						CompilationUnitDeclaration.class,
						char[].class,
						int.class,
						Map.class,
						boolean.class,
						WorkingCopyOwner.class,
						BINDING_TABLES_CLASS.get(),
						int.class,
						IProgressMonitor.class,
						boolean.class);
				convertMethod.setAccessible(true);
				return convertMethod;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("{}", e);
		}
		return null;
	};
	
	private static final Supplier<Field> HAS_COMPILATION_ABORTED_FIELD = () -> {
		try {
			Class<?> clazz = COMPILATION_UNIT_RESOLVER_CLASS.get();
			if (clazz != null) {
				Field field = clazz.getDeclaredField("hasCompilationAborted");
				field.setAccessible(true);
				return field;
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		return null;
	};
	
	static CompilationUnitDeclaration resolve(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
			List<Classpath> classpaths, Map<String, String> options, int flags, INameEnvironmentWithProgress environment)
			throws JavaModelException {
		try {

			CompilerOptions compilerOptions = (CompilerOptions) GET_COMPILER_OPTIONS_METHOD.get().invoke(null, options,
					(flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
			CancelableProblemFactory problemFactory = new CancelableProblemFactory(new NullProgressMonitor());
			boolean ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
			compilerOptions.ignoreMethodBodies = ignoreMethodBodies;
			Object resolver = COMPILATION_UNIT_RESOLVER_CONSTRUCTOR.get().newInstance(environment,
					GET_HANDLER_POLICY_METHOD.get().invoke(null), compilerOptions,
					GET_REQUESTOR_METHOD.get().invoke(null), problemFactory, new NullProgressMonitor(), false);
			boolean analyzeAndGenerateCode = !ignoreMethodBodies;
			// no existing compilation unit declaration
			CompilationUnitDeclaration unit = (CompilationUnitDeclaration) RESOLVE_METHOD.get().invoke(resolver, null,
					sourceUnit, null, true, // method verification
					analyzeAndGenerateCode, // analyze code
					analyzeAndGenerateCode); // generate code
			boolean hasCompilationAborted = HAS_COMPILATION_ABORTED_FIELD.get().getBoolean(resolver);
			if (hasCompilationAborted) {
				// the bindings could not be resolved due to missing types in name environment
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=86541
				CompilationUnitDeclaration unitDeclaration = parse(sourceUnit, options, flags);
//			if (unit != null) {
//				final int problemCount = unit.compilationResult.problemCount;
//				if (problemCount != 0) {
//					unitDeclaration.compilationResult.problems = new CategorizedProblem[problemCount];
//					System.arraycopy(unit.compilationResult.problems, 0, unitDeclaration.compilationResult.problems, 0, problemCount);
//					unitDeclaration.compilationResult.problemCount = problemCount;
//				}
//			} else if (resolver.abortProblem != null) {
//				unitDeclaration.compilationResult.problemCount = 1;
//				unitDeclaration.compilationResult.problems = new CategorizedProblem[] { resolver.abortProblem };
//			}
				return unitDeclaration;
			}
			return unit;

		} catch (Exception e) {
			log.error("{}", e);
		}
		return null;
	}
	
	static CompilationUnitDeclaration parse(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, Map<String, String> options, int flags) {
		try {
			return (CompilationUnitDeclaration) PARSE_METHOD.get()
					.invoke(null, sourceUnit, null, options, flags);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error("{}", e);
		}
		return null;
	}
	
	static CompilationUnit convert(
			CompilationUnitDeclaration compilationUnitDeclaration,
			char[] source,
			int apiLevel,
			Map<String, String> options,
			boolean needToResolveBindings,
			WorkingCopyOwner owner,
			int flags) {
		try {
			return (CompilationUnit) CONVERT_METHOD.get().invoke(null, 
					compilationUnitDeclaration,
					source,
					apiLevel,
					options,
					needToResolveBindings,
					owner,
					needToResolveBindings ? BINDING_TABLES_CONSTRUCTOR.get().newInstance() : null,
					flags,
					new NullProgressMonitor(),
					false);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| InstantiationException e) {
			log.error("{}", e);
		}
		return null;
	}
	

	static INameEnvironmentWithProgress createLookupEnvironment(Classpath[] classpath) {
		try {
			return (INameEnvironmentWithProgress) LOOKUP_ENVIRONMENT_CONSTRUCTOR.get().newInstance(classpath, null, new NullProgressMonitor());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			log.error("{}", e);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	static List<Classpath> getClasspath(ASTParser parser) {
		try {
			return (List<Classpath>) GET_CLASSPATH_METHOD.get().invoke(parser);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error("{}", e);
		}
		return null;
	}

}
