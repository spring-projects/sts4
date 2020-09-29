/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.schema.constraints;

import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;
import org.yaml.snakeyaml.nodes.Node;

/**
 * An implementations of this interface represents some 'programatic' constraint attached to a schema type.
 * Essentially, it encapsulates a verification procedure to be called upon to validate something when
 * visiting a node in the YamlAST that has been deterimed to be of that type.
 */
@FunctionalInterface
public interface Constraint {

	/**
	 * Implementors gain access to various bits of context information passed as parameters and
	 * are supposed to use this information in whatever way they like to check if the
	 * constraint is satisfied. When the constraint is not satisfied they should report any
	 * violations by adding problems to the provided {@link IProblemCollector}.
	 *
	 * @param node    The node being validated
	 * @param type   The inferred type of the node.
	 * @param foundProps The properties this node defines.
	 * @param problems  Problem collector where to which the constraint should add the validation problems it finds.
	 */
	void verify(DynamicSchemaContext dc, Node parent, Node node, YType type, IProblemCollector problems);

}
