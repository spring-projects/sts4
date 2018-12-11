/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.util.List;
import java.util.Map;

import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;

public class TypeData extends MemberData {

	private String fqName;
	private String bindingKey;
	private boolean clazz;
	private boolean annotation;
	private boolean interfaze;
	private boolean enam;
	private String superClassName;
	private String[] superInterfaceNames;
	private List<FieldData> fields;
	private List<MethodData> methods;
	private List<AnnotationData> annotations;

	private ClasspathEntryData classpathEntry;

	public String getFqName() {
		return fqName;
	}

	public void setFqName(String fqName) {
		this.fqName = fqName;
	}

	public List<FieldData> getFields() {
		return fields;
	}

	public void setFields(List<FieldData> fields) {
		this.fields = fields;
	}

	public List<MethodData> getMethods() {
		return methods;
	}

	public void setMethods(List<MethodData> methods) {
		this.methods = methods;
	}

	public String getBindingKey() {
		return bindingKey;
	}

	public void setBindingKey(String bindingKey) {
		this.bindingKey = bindingKey;
	}

	public ClasspathEntryData getClasspathEntry() {
		return classpathEntry;
	}

	public void setClasspathEntry(ClasspathEntryData classpathContainer) {
		this.classpathEntry = classpathContainer;
	}

	public boolean isClass() {
		return clazz;
	}

	public void setClass(boolean clazz) {
		this.clazz = clazz;
	}

	public boolean isAnnotation() {
		return annotation;
	}

	public void setAnnotation(boolean annotation) {
		this.annotation = annotation;
	}

	public boolean isInterface() {
		return interfaze;
	}

	public void setInterface(boolean interfaze) {
		this.interfaze = interfaze;
	}

	public boolean isEnum() {
		return enam;
	}

	public void setEnum(boolean enam) {
		this.enam = enam;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public String[] getSuperInterfaceNames() {
		return superInterfaceNames;
	}

	public void setSuperInterfaceNames(String[] superInterfaceNames) {
		this.superInterfaceNames = superInterfaceNames;
	}

	public List<AnnotationData> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationData> annotations) {
		this.annotations = annotations;
	}
	
	public static class AnnotationData extends JavaElementData {
		
		String fqName;
		
		Map<String, Object> valuePairs;

		public AnnotationData() {
		}

		public String getFqName() {
			return fqName;
		}

		public void setFqName(String fqName) {
			this.fqName = fqName;
		}

		public Map<String, Object> getValuePairs() {
			return valuePairs;
		}

		public void setValuePairs(Map<String, Object> valuePairs) {
			this.valuePairs = valuePairs;
		}

	}
	
	public static class FieldData extends MemberData {

		private String bindingKey;
		private List<AnnotationData> annotations;

		public String getBindingKey() {
			return bindingKey;
		}

		public void setBindingKey(String bindingKey) {
			this.bindingKey = bindingKey;
		}

		public List<AnnotationData> getAnnotations() {
			return annotations;
		}

		public void setAnnotations(List<AnnotationData> annotations) {
			this.annotations = annotations;
		}

	}
	
	public static class ClasspathEntryData {
		
		private String module;
		private CPE cpe;

		public String getModule() {
			return module;
		}

		public void setModule(String module) {
			this.module = module;
		}

		public CPE getCpe() {
			return cpe;
		}

		public void setCpe(CPE cpe) {
			this.cpe = cpe;
		}

	}
	
	public static class MethodData extends MemberData {

		private String bindingKey;
		private boolean constructor;
		private List<AnnotationData> annotations;

		public String getBindingKey() {
			return bindingKey;
		}

		public void setBindingKey(String bindingKey) {
			this.bindingKey = bindingKey;
		}

		public boolean isConstructor() {
			return constructor;
		}

		public void setConstructor(boolean constructor) {
			this.constructor = constructor;
		}

		public List<AnnotationData> getAnnotations() {
			return annotations;
		}

		public void setAnnotations(List<AnnotationData> annotations) {
			this.annotations = annotations;
		}

	}
}
