/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.ValueParsers;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeFactory.YTypedPropertyImpl;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;

/**
 * @author Kris De Volder
 */
public class BoshDeploymentManifestSchema implements YamlSchema {

	private final AbstractType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;

	public final YTypeFactory f = new YTypeFactory();
	public final YType t_string = f.yatomic("String");
	public final YType t_ne_string = f.yatomic("String")
			.parseWith(ValueParsers.NE_STRING);

	public final YType t_strings = f.yseq(t_string);

	public final YAtomicType t_boolean = f.yenum("boolean", "true", "false");
	public final YType t_any = f.yany("Object");
	public final YType t_params = f.ymap(t_string, t_any);
	public final YType t_string_params = f.ymap(t_string, t_string);
	public final YType t_pos_integer = f.yatomic("Positive Integer")
			.parseWith(ValueParsers.POS_INTEGER);
	public final YType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer")
			.parseWith(ValueParsers.integerAtLeast(1));

	public BoshDeploymentManifestSchema() {
		TYPE_UTIL = f.TYPE_UTIL;
		
		TOPLEVEL_TYPE = f.ybean("BoshDeploymentManifest");
		addProp(TOPLEVEL_TYPE, "name", t_ne_string);
	}

	@Override
	public YType getTopLevelType() {
		return TOPLEVEL_TYPE;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return TYPE_UTIL;
	}
	
	private YTypedPropertyImpl prop(AbstractType beanType, String name, YType type) {
		YTypedPropertyImpl prop = f.yprop(name, type);
		prop.setDescriptionProvider(descriptionFor(beanType, name));
		return prop;
	}
	
	public static Renderable descriptionFor(YType owner, String propName) {
		String typeName = owner.toString();
		return Renderables.fromClasspath(BoshDeploymentManifestSchema.class, "/desc/"+typeName+"/"+propName);
	}

	private YTypedPropertyImpl addProp(AbstractType bean, String name, YType type) {
		return addProp(bean, bean, name, type);
	}

	private YTypedPropertyImpl addProp(AbstractType superType, AbstractType bean, String name, YType type) {
		YTypedPropertyImpl p = prop(superType, name, type);
		bean.addProperty(p);
		return p;
	}

}
