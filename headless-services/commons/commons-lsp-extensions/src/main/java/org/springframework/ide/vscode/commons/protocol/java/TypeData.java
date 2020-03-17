/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;

public class TypeData extends TypeDescriptorData {

	private String bindingKey;
	private List<FieldData> fields;
	private List<MethodData> methods;
	private List<AnnotationData> annotations;
	private ClasspathEntryData classpathEntry;

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

	public List<AnnotationData> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationData> annotations) {
		this.annotations = annotations;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((bindingKey == null) ? 0 : bindingKey.hashCode());
		result = prime * result + ((classpathEntry == null) ? 0 : classpathEntry.hashCode());
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeData other = (TypeData) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (bindingKey == null) {
			if (other.bindingKey != null)
				return false;
		} else if (!bindingKey.equals(other.bindingKey))
			return false;
		if (classpathEntry == null) {
			if (other.classpathEntry != null)
				return false;
		} else if (!classpathEntry.equals(other.classpathEntry))
			return false;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (methods == null) {
			if (other.methods != null)
				return false;
		} else if (!methods.equals(other.methods))
			return false;
		return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((fqName == null) ? 0 : fqName.hashCode());
			result = prime * result + ((valuePairs == null) ? 0 : valuePairs.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnnotationData other = (AnnotationData) obj;
			if (fqName == null) {
				if (other.fqName != null)
					return false;
			} else if (!fqName.equals(other.fqName))
				return false;
			if (valuePairs == null) {
				if (other.valuePairs != null)
					return false;
			} else if (!valuePairs.equals(other.valuePairs))
				return false;
			return true;
		}

	}
	
	public static class FieldData extends MemberData {

		private String bindingKey;
		private JavaTypeData type;
		private boolean enumConstant;
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

		public JavaTypeData getType() {
			return type;
		}

		public void setType(JavaTypeData type) {
			this.type = type;
		}

		public boolean isEnumConstant() {
			return enumConstant;
		}

		public void setEnumConstant(boolean enumConstant) {
			this.enumConstant = enumConstant;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
			result = prime * result + ((bindingKey == null) ? 0 : bindingKey.hashCode());
			result = prime * result + (enumConstant ? 1231 : 1237);
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldData other = (FieldData) obj;
			if (annotations == null) {
				if (other.annotations != null)
					return false;
			} else if (!annotations.equals(other.annotations))
				return false;
			if (bindingKey == null) {
				if (other.bindingKey != null)
					return false;
			} else if (!bindingKey.equals(other.bindingKey))
				return false;
			if (enumConstant != other.enumConstant)
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cpe == null) ? 0 : cpe.hashCode());
			result = prime * result + ((module == null) ? 0 : module.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClasspathEntryData other = (ClasspathEntryData) obj;
			if (cpe == null) {
				if (other.cpe != null)
					return false;
			} else if (!cpe.equals(other.cpe))
				return false;
			if (module == null) {
				if (other.module != null)
					return false;
			} else if (!module.equals(other.module))
				return false;
			return true;
		}

	}
	
	public static class MethodData extends MemberData {

		private String bindingKey;
		private boolean constructor;
		private JavaTypeData returnType;
		private List<JavaTypeData> parameters;
		private List<String> parameterNames;
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

		public JavaTypeData getReturnType() {
			return returnType;
		}

		public void setReturnType(JavaTypeData returnType) {
			this.returnType = returnType;
		}
		
		public List<JavaTypeData> getParameters() {
			return parameters;
		}

		public void setParameters(List<JavaTypeData> parameters) {
			this.parameters = parameters;
		}

		public List<String> getParameterNames() {
			return parameterNames;
		}

		public void setParameterNames(List<String> parameterNames) {
			this.parameterNames = parameterNames;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
			result = prime * result + ((bindingKey == null) ? 0 : bindingKey.hashCode());
			result = prime * result + (constructor ? 1231 : 1237);
			result = prime * result + ((parameterNames == null) ? 0 : parameterNames.hashCode());
			result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
			result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodData other = (MethodData) obj;
			if (annotations == null) {
				if (other.annotations != null)
					return false;
			} else if (!annotations.equals(other.annotations))
				return false;
			if (bindingKey == null) {
				if (other.bindingKey != null)
					return false;
			} else if (!bindingKey.equals(other.bindingKey))
				return false;
			if (constructor != other.constructor)
				return false;
			if (parameterNames == null) {
				if (other.parameterNames != null)
					return false;
			} else if (!parameterNames.equals(other.parameterNames))
				return false;
			if (parameters == null) {
				if (other.parameters != null)
					return false;
			} else if (!parameters.equals(other.parameters))
				return false;
			if (returnType == null) {
				if (other.returnType != null)
					return false;
			} else if (!returnType.equals(other.returnType))
				return false;
			return true;
		}
	}
}
