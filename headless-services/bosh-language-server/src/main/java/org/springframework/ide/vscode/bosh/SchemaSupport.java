/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YTypedPropertyImpl;

/**
 * Some common utility methods useful in implementing Schemas.
 *
 * @author Kris De Volder
 */
public class SchemaSupport {

	protected final YTypeFactory f;

	protected SchemaSupport(YTypeFactory f) {
		this.f = f;
	}

	protected String getResourcePathPrefix() {
		return "/desc/";
	}

	protected YTypedPropertyImpl prop(AbstractType beanType, String name, YType type) {
		YTypedPropertyImpl prop = f.yprop(name, type);
		prop.setDescriptionProvider(descriptionFor(beanType, name));
		return prop;
	}

	protected Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(SchemaSupport.class, getResourcePathPrefix()+typeName+"/"+propName);
	}

	protected YTypedPropertyImpl addProp(AbstractType bean, String name, YType type) {
		return addProp(bean, bean, name, type);
	}

	protected YTypedPropertyImpl addProp(AbstractType superType, AbstractType bean, String name, YType type) {
		YTypedPropertyImpl p = prop(superType, name, type);
		bean.addProperty(p);
		return p;
	}


}
