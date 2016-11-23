package org.springframework.ide.vscode.application.properties.hover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo;
import org.springframework.ide.vscode.application.properties.metadata.PropertyInfo.PropertySource;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.StringUtil;


/**
 * Information object that is displayed in SpringPropertiesTextHover's information
 * control.
 * <p>
 * Essentially this is a wrapper around {@link ConfigurationMetadataProperty}
 *
 * @author Kris De Volder
 */
public class PropertyRenderableProvider extends AbstractPropertyRenderableProvider {

	/**
	 * Java project which is used to find declaration for 'navigate to declaration' action
	 */
	private IJavaProject javaProject;

	/**
	 * Data object to display in 'hover text'
	 */
	private PropertyInfo data;

	public PropertyRenderableProvider(IJavaProject project, PropertyInfo data) {
		this.javaProject = project;
		this.data = data;
	}

	public PropertyInfo getElement() {
		return data;
	}

	public boolean canOpenDeclaration() {
		return getJavaElements()!=null;
	}

	/**
	 * Like 'getSources' but converts raw info into IJavaElements. Raw data which fails to be converted
	 * is silenetly ignored.
	 */
	public List<IJavaElement> getJavaElements() {
		try {
			if (javaProject!=null) {
				List<PropertySource> sources = getSources();
				if (!sources.isEmpty()) {
					ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>();
					for (PropertySource source : sources) {
						String typeName = source.getSourceType();
						if (typeName!=null) {
							IType type = javaProject.findType(typeName);
							IMethod method = null;
							if (type!=null) {
								String methodSig = source.getSourceMethod();
								if (methodSig!=null) {
									method = getMethod(type, methodSig);
								} else {
									method = getSetter(type, getElement());
								}
							}
							if (method!=null) {
								elements.add(method);
							} else if (type!=null) {
								elements.add(type);
							}
						}
					}
					return elements;
				}
			} else {
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Attempt to find corresponding setter method for a given property.
	 * @return setter method, or null if not found.
	 */
	private IMethod getSetter(IType type, PropertyInfo propertyInfo) {
		try {
			String propName = propertyInfo.getName();
			String setterName = "set"
				+Character.toUpperCase(propName.charAt(0))
				+toCamelCase(propName.substring(1));
			String sloppySetterName = setterName.toLowerCase();

			IMethod sloppyMatch = null;
			for (IMethod m : type.getMethods().collect(Collectors.toList())) {
				String mname = m.getElementName();
				if (setterName.equals(mname)) {
					//found 'exact' name match... done
					return m;
				} else if (mname.toLowerCase().equals(sloppySetterName)) {
					sloppyMatch = m;
				}
			}
			return sloppyMatch;
		} catch (Exception e) {
			Log.log(e);
			return null;
		}
	}

	/**
	 * Convert hyphened name to camel case name. It is
	 * safe to call this on an already camel-cased name.
	 */
	private String toCamelCase(String name) {
		if (name.isEmpty()) {
			return name;
		} else {
			StringBuilder camel = new StringBuilder();
			char[] chars = name.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c=='-') {
					i++;
					if (i<chars.length) {
						camel.append(Character.toUpperCase(chars[i]));
					}
				} else {
					camel.append(chars[i]);
				}
			}
			return camel.toString();
		}
	}

	/**
	 * Get 'raw' info about sources that define this property.
	 */
	public List<PropertySource> getSources() {
		return data.getSources();
	}

	private IMethod getMethod(IType type, String methodSig) {
		int nameEnd = methodSig.indexOf('(');
		String name;
		if (nameEnd>=0) {
			name = methodSig.substring(0, nameEnd);
		} else {
			name = methodSig;
		}
		//TODO: This code assumes 0 arguments, which is the case currently for all
		//  'real' data in spring jars.
		IMethod m = type.getMethod(name, Stream.empty());
		if (m!=null) {
			return m;
		}
		//try  find a method  with the same name.
		return type.getMethods().filter(meth -> name.equals(meth.getElementName())).findFirst().orElse(null);
	}

	@Override
	protected Object getDefaultValue() {
		return data.getDefaultValue();
	}

	@Override
	protected IJavaProject getJavaProject() {
		return javaProject;
	}

	@Override
	protected Renderable getDescription() {
		String desc = data.getDescription();
		if (StringUtil.hasText(desc)) {
			return Renderables.text(desc);
		}
		return null;
	}

	@Override
	protected String getType() {
		return data.getType();
	}

	@Override
	protected String getDeprecationReason() {
		return data.getDeprecationReason();
	}

	@Override
	protected String getId() {
		return data.getId();
	}

	@Override
	protected String getDeprecationReplacement() {
		return data.getDeprecationReplacement();
	}

	@Override
	protected boolean isDeprecated() {
		return data.isDeprecated();
	}

}
