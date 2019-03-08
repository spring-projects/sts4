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
package org.springframework.ide.vscode.bosh.models;

import java.util.function.Predicate;

import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.yaml.snakeyaml.nodes.Node;

/**
 * A 'view' on a Yaml AST node interpreting it as a Stemcell declaration.
 */
public class StemcellModel {

	public static final Predicate<StemcellData> ALLWAYS_TRUE_FILTER = new Predicate<StemcellData>() {
			@Override
			public boolean test(StemcellData sc) {
				return true;
			}
			@Override
			public String toString() {
				return "true";
			}
	};
	private Node node;

	public StemcellModel(Node node) {
		this.node = node;
	}

	public String getName() {
		return NodeUtil.getScalarProperty(node, "name");
	}

	public String getOs() {
		return NodeUtil.getScalarProperty(node, "os");
	}

	/**
	 * Create an appropriate filter to limit the candidate stemcells for
	 * providing a version based on 'name' or 'os' values already defined in this stemcell.
	 */
	public Predicate<StemcellData> createVersionFilter() {
		String name = getName();
		String os = getOs();
		if (StringUtil.hasText(name) && StringUtil.hasText(os)) {
			//User shouldn't really specify both of these... but if they do... then
			// let's do our best to generate proposals for the both of them.
			return new Predicate<StemcellData>() {
				@Override
				public boolean test(StemcellData sc) {
					return 	name.equals(sc.getName()) || os.equals(sc.getOs());
				}

				@Override
				public String toString() {
					return "name="+name+", os="+os;
				}
			};
		} else if (StringUtil.hasText(name)) {
			return new Predicate<StemcellData>() {
				@Override
				public boolean test(StemcellData sc) {
					return name.equals(sc.getName());
				}
				@Override
				public String toString() {
					return "name="+name;
				}
			};
		} else if (StringUtil.hasText(os)) {
			return new Predicate<StemcellData>() {
				@Override
				public boolean test(StemcellData sc) {
					return os.equals(sc.getOs());
				}
				@Override
				public String toString() {
					return "os="+os;
				}
			};
		} else {
			return ALLWAYS_TRUE_FILTER;
		}
	}

}
