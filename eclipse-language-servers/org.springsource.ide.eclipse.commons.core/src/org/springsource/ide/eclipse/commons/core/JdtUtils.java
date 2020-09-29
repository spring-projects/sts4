// COPIED from spring-ide org.springframework.ide.eclipse.core.java.JdtUtils
/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;


/**
 * Utility class that provides several helper methods for working with Eclipse's
 * JDT.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Leo Dos Santos
 * @since 2.0
 */
public class JdtUtils {

	public static final String CLASS_FILE_EXTENSION = ".class";

	public static final String GROOVY_FILE_EXTENSION = ".groovy";

	public static final String JAVA_FILE_EXTENSION = ".java";

	private static final String AJDT_CLASS = "org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager";

	private static final String AJDT_NATURE = "org.eclipse.ajdt.ui.ajnature";

	private static final String CLASSPATH_FILENAME = ".classpath";

	private static final boolean IS_AJDT_PRESENT = isAjdtPresent();

	/** Suffix for array class names: "[]" */
	private static final String ARRAY_SUFFIX = "[]";

	/**
	 * Creates specified Java project.
	 */
	public static IJavaProject createJavaProject(String projectName, IProgressMonitor monitor) throws CoreException {
		IProject project = SpringCoreUtils.createProject(projectName, null, monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			SpringCoreUtils.addProjectNature(project, JavaCore.NATURE_ID, monitor);
		}
		IJavaProject jproject = JavaCore.create(project);
		// append JRE entry
		jproject.setRawClasspath(new IClasspathEntry[] { getJreVariableEntry() }, monitor);
		jproject.setOutputLocation(project.getFullPath(), monitor);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return jproject;
	}

	/**
	 * Checks if the given <code>type</code> implements/extends
	 * <code>className</code>.
	 */
	// public static boolean doesImplement(IResource resource, IType type,
	// String className) {
	// if (resource == null || type == null || className == null) {
	// return false;
	// }
	// if (className.startsWith("java.") || className.startsWith("javax.")) {
	// try {
	// ClassLoader cls = getClassLoader(resource.getProject(), null);
	// Class<?> typeClass = cls.loadClass(type.getFullyQualifiedName('$'));
	// Class<?> interfaceClass = cls.loadClass(className);
	// return typeClass.equals(interfaceClass) ||
	// interfaceClass.isAssignableFrom(typeClass);
	// }
	// catch (Throwable e) {
	// // ignore this and fall back to JDT does implement checks
	// }
	// }
	// return doesImplementWithJdt(resource, type, className);
	// }

	// public static IType getAjdtType(IProject project, String className) {
	// IJavaProject javaProject = getJavaProject(project);
	// if (IS_AJDT_PRESENT && javaProject != null && className != null) {
	//
	// try {
	// IType type = null;
	//
	// // First look for the type in the project
	// if (isAjdtProject(project)) {
	// type = AjdtUtils.getAjdtType(project, className);
	// if (type != null) {
	// return type;
	// }
	// }
	//
	// // Then look for the type in the referenced Java projects
	// for (IProject refProject : project.getReferencedProjects()) {
	// if (isAjdtProject(refProject)) {
	// type = AjdtUtils.getAjdtType(refProject, className);
	// if (type != null) {
	// return type;
	// }
	// }
	// }
	// }
	// catch (CoreException e) {
	// StatusHandler.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
	// "Error getting Java type '"
	// + className + "'", e));
	// }
	// }
	// return null;
	// }

	public static List<IJavaProject> getAllDependingJavaProjects(IJavaProject project) {
		List<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
		IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		if (model != null) {
			try {
				String[] names = project.getRequiredProjectNames();
				IJavaProject[] projects = model.getJavaProjects();
				for (IJavaProject project2 : projects) {
					for (String name2 : names) {
						String name = project2.getProject().getName();
						if (name.equals(name2)) {
							javaProjects.add(project2);
						}
					}
				}
			}
			catch (JavaModelException exception) {
			}
		}
		return javaProjects;
	}

	/**
	 * Creates a Set of {@link URL}s from the OSGi bundle class path manifest
	 * entry.
	 */
	// public static Set<URL> getBundleClassPath(String bundleId) {
	// Set<URL> paths = new HashSet<URL>();
	// try {
	// Bundle bundle = Platform.getBundle(bundleId);
	// if (bundle != null) {
	// String bundleClassPath = (String) bundle.getHeaders()
	// .get(org.osgi.framework.Constants.BUNDLE_CLASSPATH);
	// if (bundleClassPath != null) {
	// String[] classPathEntries =
	// StringUtils.delimitedListToStringArray(bundleClassPath, ",");
	// for (String classPathEntry : classPathEntries) {
	// if (".".equals(classPathEntry.trim())) {
	// paths.add(FileLocator.toFileURL(bundle.getEntry("/")));
	// }
	// else {
	// paths.add(FileLocator.toFileURL(new URL(bundle.getEntry("/"), "/" +
	// classPathEntry.trim())));
	// }
	// }
	// }
	// else {
	// paths.add(FileLocator.toFileURL(bundle.getEntry("/")));
	// }
	// }
	// }
	// catch (MalformedURLException e) {
	// StatusHandler
	// .log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
	// "An error occurred getting classpath", e));
	// }
	// catch (IOException e) {
	// StatusHandler
	// .log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
	// "An error occurred getting classpath", e));
	// }
	// return paths;
	// }

	/**
	 * @since 2.6.0
	 */
	public static IJavaElement getByHandle(String handle) {
		// if (IS_AJDT_PRESENT) {
		// return AjdtUtils.getByHandle(handle);
		// }
		// else {
		return JavaCore.create(handle);
		// }
	}

	/**
	 * Create a {@link ClassLoader} from the class path configuration of the
	 * given <code>project</code>.
	 * @param project the {@link IProject}
	 * @param useParentClassLoader true if the current OSGi class loader should
	 * be used as parent class loader for the constructed class loader.
	 * @return {@link ClassLoader} instance constructed from the
	 * <code>project</code>'s build path configuration
	 */
	// public static ClassLoader getClassLoader(IProject project, ClassLoader
	// parentClassLoader) {
	// return ProjectClassLoaderCache.getClassLoader(project,
	// parentClassLoader);
	// }
	//
	// public static void removeClassLoaderEntryFromCache(IProject project) {
	// ProjectClassLoaderCache.removeClassLoaderEntryFromCache(project);
	// }
	//
	// public static IMethod getConstructor(IType type, Class[] parameterTypes)
	// {
	// String[] parameterTypesAsString =
	// getParameterTypesAsStringArray(parameterTypes);
	// return getConstructor(type, parameterTypesAsString);
	// }

	// public static IMethod getConstructor(IType type, String[] parameterTypes)
	// {
	// try {
	// Set<IMethod> methods = Introspector.getAllConstructors(type);
	// for (IMethod method : methods) {
	// if (method.getParameterTypes().length == parameterTypes.length) {
	// String[] methodParameterTypes = getParameterTypesAsStringArray(method);
	// if (Arrays.deepEquals(parameterTypes, methodParameterTypes)) {
	// return method;
	// }
	// }
	// }
	// }
	// catch (JavaModelException e) {
	// }
	// return null;
	// }

	// public static IField getField(IType type, String fieldName, String
	// parameterType) {
	// try {
	// Set<IField> methods = Introspector.getAllFields(type);
	// for (IField field : methods) {
	// if (field.getElementName().equals(fieldName)) {
	// return field;
	// }
	// }
	// }
	// catch (JavaModelException e) {
	// }
	// return null;
	// }

	/**
	 * Returns a flat list of all interfaces and super types for the given
	 * {@link IType}.
	 */
	// public static List<String> getFlatListOfClassAndInterfaceNames(IType
	// parameterType, IType type) {
	// List<String> requiredTypes = new ArrayList<String>();
	// if (parameterType != null) {
	// do {
	// try {
	// requiredTypes.add(parameterType.getFullyQualifiedName());
	// String[] interfaceNames = parameterType.getSuperInterfaceNames();
	// for (String interfaceName : interfaceNames) {
	// if (interfaceName != null) {
	// if (type.isBinary()) {
	// requiredTypes.add(interfaceName);
	// }
	// String resolvedName = resolveClassName(interfaceName, type);
	// if (resolvedName != null) {
	// requiredTypes.add(resolvedName);
	// }
	// }
	// }
	// parameterType = Introspector.getSuperType(parameterType);
	// }
	// catch (JavaModelException e) {
	// }
	// } while (parameterType != null &&
	// !parameterType.getFullyQualifiedName().equals(Object.class.getName()));
	// }
	// return requiredTypes;
	// }

	/**
	 * Returns the corresponding Java project or <code>null</code> a for given
	 * project.
	 * @param project the project the Java project is requested for
	 * @return the requested Java project or <code>null</code> if the Java
	 * project is not defined or the project is not accessible
	 */
	public static IJavaProject getJavaProject(IProject project) {
		if (project.isAccessible()) {
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				}
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
						"Error getting Java project for project '" + project.getName() + "'", e));
			}
		}
		return null;
	}

	public static IJavaProject getJavaProject(IResource config) {
		IJavaProject project = JavaCore.create(config.getProject());
		return project;
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * @param project the JDT project the class belongs to
	 * @param className the full qualified class name of the requested Java type
	 * @return the requested Java type or null if the class is not defined or
	 * the project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (className != null) {
			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0) {
				className = className.replace('$', '.');
			}
			try {
				IType type = null;
				// First look for the type in the Java project
				if (javaProject != null) {
					// TODO CD not sure why we need
					type = javaProject.findType(className, new NullProgressMonitor());
					// type = javaProject.findType(className);
					if (type != null) {
						return type;
					}
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					IJavaProject refJavaProject = JdtUtils.getJavaProject(refProject);
					if (refJavaProject != null) {
						type = refJavaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
				}

				// fall back and try to locate the class using AJDT
				// TODO: uncomment this call
				// return getAjdtType(project, className);
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Error getting Java type '"
						+ className + "'", e));
			}
		}

		return null;
	}

	public static final IType getJavaTypeForMethodReturnType(IMethod method, IType contextType) {
		try {
			return JdtUtils.getJavaTypeFromSignatureClassName(method.getReturnType(), contextType);
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static IType getJavaTypeFromSignatureClassName(String className, IType contextType) {
		if (contextType == null || className == null) {
			return null;
		}
		try {
			return JdtUtils.getJavaType(contextType.getJavaProject().getProject(),
					JdtUtils.resolveClassNameBySignature(className, contextType));
		}
		catch (IllegalArgumentException e) {
			// do Nothing
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static final List<IType> getJavaTypesForMethodParameterTypes(IMethod method, IType contextType) {
		if (method == null || method.getParameterTypes() == null || method.getParameterTypes().length == 0) {
			return Collections.EMPTY_LIST;
		}
		List<IType> parameterTypes = new ArrayList<IType>(method.getParameterTypes().length);
		String[] parameterTypeNames = method.getParameterTypes();
		for (String parameterTypeName : parameterTypeNames) {
			parameterTypes.add(JdtUtils.getJavaTypeFromSignatureClassName(parameterTypeName, contextType));
		}
		return parameterTypes;
	}

	public static IClasspathEntry getJreVariableEntry() {
		return JavaRuntime.getDefaultJREContainerEntry();
	}

	public static int getLineNumber(IJavaElement element) {
		if (element != null && element instanceof IMethod) {
			try {
				IMethod method = (IMethod) element;
				int lines = 0;
				if (method.getDeclaringType() != null && method.getDeclaringType().getCompilationUnit() != null) {
					String targetsource = method.getDeclaringType().getCompilationUnit().getSource();
					if (targetsource != null) {
						String sourceuptomethod = targetsource.substring(0, method.getNameRange().getOffset());

						char[] chars = new char[sourceuptomethod.length()];
						sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
						for (char element0 : chars) {
							if (element0 == '\n') {
								lines++;
							}
						}
						return new Integer(lines + 1);
					}
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IType && ((IType) element).getCompilationUnit() != null) {
			try {
				IType type = (IType) element;
				int lines = 0;
				String targetsource = type.getCompilationUnit().getSource();
				if (targetsource != null) {
					String sourceuptomethod = targetsource.substring(0, type.getNameRange().getOffset());

					char[] chars = new char[sourceuptomethod.length()];
					sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
					for (char element0 : chars) {
						if (element0 == '\n') {
							lines++;
						}
					}
					return new Integer(lines + 1);
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (element != null && element instanceof IField) {
			try {
				IField type = (IField) element;
				int lines = 0;
				ICompilationUnit cu = type.getCompilationUnit();
				if (cu != null) {
					String targetsource = cu.getSource();
					if (targetsource != null) {
						String sourceuptomethod = targetsource.substring(0, type.getNameRange().getOffset());

						char[] chars = new char[sourceuptomethod.length()];
						sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
						for (char element0 : chars) {
							if (element0 == '\n') {
								lines++;
							}
						}
						return new Integer(lines + 1);
					}
				}
			}
			catch (JavaModelException e) {
			}
		}
		return new Integer(-1);
	}

	// public static IMethod getMethod(IType type, String methodName, Class[]
	// parameterTypes) {
	// String[] parameterTypesAsString =
	// getParameterTypesAsStringArray(parameterTypes);
	// return getMethod(type, methodName, parameterTypesAsString);
	// }

	// public static IMethod getMethod(IType type, String methodName, String[]
	// parameterTypes) {
	// int index = methodName.indexOf('(');
	// if (index >= 0) {
	// methodName = methodName.substring(0, index);
	// }
	// try {
	// Set<IMethod> methods = Introspector.getAllMethods(type);
	// for (IMethod method : methods) {
	// if (method.getElementName().equals(methodName)
	// && method.getParameterTypes().length == parameterTypes.length) {
	// String[] methodParameterTypes = getParameterTypesAsStringArray(method);
	// if (Arrays.deepEquals(parameterTypes, methodParameterTypes)) {
	// return method;
	// }
	// }
	// }
	//
	// return Introspector.findMethod(type, methodName, parameterTypes.length,
	// Public.YES, Static.DONT_CARE);
	// }
	// catch (JavaModelException e) {
	// }
	// return null;
	// }

	public static String getMethodName(IMethod method) {
		// Special support Ajdt intertype declarations
		String methodName = method.getElementName();
		int index = methodName.lastIndexOf('.');
		if (index > 0) {
			methodName = methodName.substring(index + 1);
		}
		return methodName;
	}

	public static String[] getParameterTypesString(IMethod method) {
		try {
			String[] parameterQualifiedTypes = Signature.getParameterTypes(method.getSignature());
			int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
			String[] parameterPackages = new String[length];
			for (int i = 0; i < length; i++) {
				parameterQualifiedTypes[i] = parameterQualifiedTypes[i].replace('/', '.');
				parameterPackages[i] = Signature.getSignatureSimpleName(parameterQualifiedTypes[i]);
			}
			return parameterPackages;
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static String getParentName(IMethod method) {
		// Special support Ajdt intertype declarations
		String methodName = method.getElementName();
		int index = methodName.lastIndexOf('.');
		if (index > 0) {
			return methodName.substring(0, index);
		}
		else {
			return method.getParent().getElementName();
		}
	}

	// public static IProjectClassLoaderSupport
	// getProjectClassLoaderSupport(IProject je, ClassLoader parentClassLoader)
	// {
	// return new DefaultProjectClassLoaderSupport(je, parentClassLoader);
	// }

	public static String getPropertyNameFromMethodName(IMethod method) {
		// Special support Ajdt intertype declarations
		String methodName = method.getElementName();
		int index = methodName.lastIndexOf('.');
		if (index > 0) {
			methodName = methodName.substring(index + 1);
		}
		String replaceText = methodName.substring("set".length());
		if (replaceText != null) {
			replaceText = java.beans.Introspector.decapitalize(replaceText);
		}
		return replaceText;
	}

	public static String getReturnTypeString(IMethod method, boolean classTypesOnly) {
		try {
			String qualifiedReturnType = Signature.getReturnType(method.getSignature());
			if (!classTypesOnly || qualifiedReturnType.startsWith("L") || qualifiedReturnType.startsWith("Q")) {
				return Signature.getSignatureSimpleName(qualifiedReturnType.replace('/', '.'));
			}
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static IResource getSourceResource(IResource classFile) {
		try {
			if (isJavaProject(classFile) && classFile.getName().endsWith(CLASS_FILE_EXTENSION)) {
				IPath classFilePath = classFile.getFullPath();
				String classFileName = null;

				IJavaProject project = getJavaProject(classFile);
				IPath defaultOutput = project.getOutputLocation();

				if (defaultOutput.isPrefixOf(classFilePath)) {
					classFileName = classFilePath.removeFirstSegments(defaultOutput.segmentCount()).toString();
				}
				else {
					for (IClasspathEntry entry : project.getRawClasspath()) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath output = entry.getOutputLocation();
							if (output != null) {
								if (classFilePath.isPrefixOf(output)) {
									classFileName = classFilePath.removeFirstSegments(output.segmentCount()).toString();
								}
							}
						}
					}
				}

				if (classFileName != null) {
					// Replace file extension
					String sourceFileName = classFileName.replace(".class", ".java");
					for (IClasspathEntry entry : project.getRawClasspath()) {
						if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath path = entry.getPath().append(sourceFileName).removeFirstSegments(1);
							IResource resource = project.getProject().findMember(path);
							if (resource != null) {
								return resource;
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	public static boolean isAjdtPresent() {
		try {
			Class.forName(AJDT_CLASS);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns true if given resource's project is a ADJT project.
	 */
	public static boolean isAjdtProject(IResource resource) {
		return SpringCoreUtils.hasNature(resource, AJDT_NATURE);
	}

	/**
	 * Determines if the <code>resource</code> under question is the .classpath
	 * file of a {@link IJavaProject}.
	 */
	public static boolean isClassPathFile(IResource resource) {
		String classPathFileName = resource.getProject().getFullPath().append(CLASSPATH_FILENAME).toString();
		return resource.getFullPath().toString().equals(classPathFileName);
	}

	/**
	 * Returns true if given resource's project is a Java project.
	 */
	public static boolean isJavaProject(IResource resource) {
		return SpringCoreUtils.hasNature(resource, JavaCore.NATURE_ID);
	}

	// public static boolean isTypeAjdtElement(IType type) {
	// if (IS_AJDT_PRESENT) {
	// return AjdtUtils.isTypeAjdtElement(type);
	// }
	// return false;
	// }

	public static boolean isTypeGroovyElement(IType type) {
		// TODO CD verify following check with Groovy Eclipse
		ICompilationUnit cu = type.getCompilationUnit();
		if (cu != null && cu.getResource() != null) {
			return cu.getResource().getName().endsWith(GROOVY_FILE_EXTENSION);
		}
		else if (cu != null) {
			try {
				IResource resource = cu.getUnderlyingResource();
				if (resource != null) {
					return resource.getName().endsWith(GROOVY_FILE_EXTENSION);
				}
			}
			catch (JavaModelException e) {
				// ignore
			}
		}
		return false;
	}

	public static String resolveClassName(String className, IType type) {
		if (className == null || type == null) {
			return className;
		}
		// replace binary $ inner class name syntax with . for source level
		className = className.replace('$', '.');
		String dotClassName = new StringBuilder().append('.').append(className).toString();

		IProject project = type.getJavaProject().getProject();

		try {
			// Special handling for some well-know classes
			if (className.startsWith("java.lang") && getJavaType(project, className) != null) {
				return className;
			}

			// Check if the class is imported
			if (!type.isBinary()) {

				// Strip className to first segment to support
				// ReflectionUtils.MethodCallback
				int ix = className.lastIndexOf('.');
				String firstClassNameSegment = className;
				if (ix > 0) {
					firstClassNameSegment = className.substring(0, ix);
				}

				// Iterate the imports
				for (IImportDeclaration importDeclaration : type.getCompilationUnit().getImports()) {
					String importName = importDeclaration.getElementName();
					// Wildcard imports -> check if the package + className is a
					// valid type
					if (importDeclaration.isOnDemand()) {
						String newClassName = new StringBuilder(importName.substring(0, importName.length() - 1))
								.append(className).toString();
						if (getJavaType(project, newClassName) != null) {
							return newClassName;
						}
					}
					// Concrete import matching .className at the end -> check
					// if type exists
					else if (importName.endsWith(dotClassName) && getJavaType(project, importName) != null) {
						return importName;
					}
					// Check if className is multi segmented
					// (ReflectionUtils.MethodCallback)
					// -> check if the first segment
					else if (!className.equals(firstClassNameSegment)) {
						if (importName.endsWith(firstClassNameSegment)) {
							String newClassName = new StringBuilder(importName.substring(0,
									importName.lastIndexOf('.') + 1)).append(className).toString();
							if (getJavaType(project, newClassName) != null) {
								return newClassName;
							}
						}
					}
				}
			}

			// Check if the class is in the same package as the type
			String packageName = type.getPackageFragment().getElementName();
			String newClassName = new StringBuilder(packageName).append(dotClassName).toString();
			if (getJavaType(project, newClassName) != null) {
				return newClassName;
			}

			// Check if the className is sufficient (already fully-qualified)
			if (getJavaType(project, className) != null) {
				return className;
			}

			// Check if the class is coming from the java.lang
			newClassName = new StringBuilder("java.lang").append(dotClassName).toString();
			if (getJavaType(project, newClassName) != null) {
				return newClassName;
			}

			// Fall back to full blown resolution
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "An error occurred resolving class name",
					e));
		}

		return className;
	}

	public static String resolveClassNameBySignature(String className, IType type) {
		className = Signature.toString(className).replace('$', '.');
		return resolveClassName(className, type);
	}

	public static void visitTypeAst(IType type, ASTVisitor visitor) {
		if (type != null && type.getCompilationUnit() != null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(type.getCompilationUnit());
			parser.setResolveBindings(true);
			ASTNode node = parser.createAST(new NullProgressMonitor());
			node.accept(visitor);
		}
	}

	// private static boolean doesImplementWithJdt(IResource resource, IType
	// type, String className) {
	// IType interfaceType = getJavaType(resource.getProject(), className);
	// if (type != null && interfaceType != null) {
	// try {
	// IType[] subTypes =
	// SuperTypeHierarchyCache.getTypeHierarchy(interfaceType)
	// .getAllSubtypes(interfaceType);
	// if (subTypes != null) {
	// for (IType subType : subTypes) {
	// if (subType.equals(type)) {
	// return true;
	// }
	// }
	// }
	// }
	// catch (JavaModelException ex) {
	// StatusHandler.log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
	// "An error occurred resolving subtypes", ex));
	// }
	// }
	// return false;
	// }

	// private static String[] getParameterTypesAsStringArray(Class[]
	// parameterTypes) {
	// String[] parameterTypesAsString = new String[parameterTypes.length];
	// for (int i = 0; i < parameterTypes.length; i++) {
	// parameterTypesAsString[i] =
	// ClassUtils.getQualifiedName(parameterTypes[i]);
	// }
	// return parameterTypesAsString;
	// }

	private static String[] getParameterTypesAsStringArray(IMethod method) {
		Set<String> typeParameterNames = new HashSet<String>();
		try {
			for (ITypeParameter param : method.getDeclaringType().getTypeParameters()) {
				typeParameterNames.add(param.getElementName());
			}
			for (ITypeParameter param : method.getTypeParameters()) {
				typeParameterNames.add(param.getElementName());
			}
		}
		catch (JavaModelException e) {
		}
		String[] parameterTypesAsString = new String[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			String parameterTypeString = Signature.getElementType(method.getParameterTypes()[i]);
			boolean isArray = !parameterTypeString.equals(method.getParameterTypes()[i]);

			String parameterType = resolveClassNameBySignature(parameterTypeString, method.getDeclaringType());
			if (typeParameterNames.contains(parameterType)) {
				parameterTypesAsString[i] = Object.class.getName() + (isArray ? ARRAY_SUFFIX : "");
			}
			else {
				parameterTypesAsString[i] = parameterType + (isArray ? ARRAY_SUFFIX : "");
			}
		}
		return parameterTypesAsString;
	}

	// static class DefaultProjectClassLoaderSupport implements
	// IProjectClassLoaderSupport {
	//
	// private ClassLoader classLoader;
	//
	// private ClassLoader weavingClassLoader;
	//
	// public DefaultProjectClassLoaderSupport(IProject javaProject, ClassLoader
	// parentClassLoader) {
	// setupClassLoaders(javaProject, parentClassLoader);
	// }
	//
	// public void executeCallback(IProjectClassLoaderAwareCallback callback)
	// throws Throwable {
	// try {
	// activateWeavingClassLoader();
	// callback.doWithActiveProjectClassLoader();
	// }
	// finally {
	// recoverClassLoader();
	// }
	// }
	//
	// public ClassLoader getProjectClassLoader() {
	// return this.weavingClassLoader;
	// }
	//
	// /**
	// * Activates the weaving class loader as thread context classloader.
	// * <p>
	// * Use {@link #recoverClassLoader()} to recover the original thread
	// * context classloader
	// */
	// private void activateWeavingClassLoader() {
	// Thread.currentThread().setContextClassLoader(weavingClassLoader);
	// }
	//
	// private void recoverClassLoader() {
	// Thread.currentThread().setContextClassLoader(classLoader);
	// }
	//
	// private void setupClassLoaders(IProject project, ClassLoader
	// parentClassLoader) {
	// classLoader = Thread.currentThread().getContextClassLoader();
	// weavingClassLoader = ProjectClassLoaderCache.getClassLoader(project,
	// parentClassLoader);
	// }
	// }

}
