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
package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.commons.util.ValueParser;
import org.springframework.ide.vscode.commons.yaml.schema.constraints.Constraint;

/**
 * An implementation of YTypeUtil provides implementations of various
 * methods operating on YTypes and interpreting them in some context
 * (e.g. the meaning of YType objects may depend on types resolved
 * from the current project's classpath).
 *
 * @author Kris De Volder
 */
public interface YTypeUtil {
	boolean isAtomic(YType type);
	boolean isMap(YType type);
	boolean isSequencable(YType type);
	boolean isBean(YType type);
	YType getDomainType(YType type);
	YValueHint[] getHintValues(YType yType, DynamicSchemaContext dc) throws Exception;
	String niceTypeName(YType type);
	YType getKeyType(YType type);
	SchemaContextAware<ValueParser> getValueParser(YType type);

	//TODO: only one of these two should be enough?
	List<YTypedProperty> getProperties(YType type);
	Map<String, YTypedProperty> getPropertiesMap(YType yType);

	/**
	 * Given a {@link DynamicSchemaContext} attempt to get a more specific type, as
	 * may be inferred by stuff present in the context. If not enough information is
	 * present in the context to narrow the type, then the type itself
	 * should be returned.
	 */
	YType inferMoreSpecificType(YType type, DynamicSchemaContext dc);
	List<Constraint> getConstraints(YType type);
	
	ISubCompletionEngine getCustomContentAssistant(YType type);
	
	/**
	 * Config option for type-based completion engine. This enables the
	 * 'tiered' proposals feature (so that optional properties are not
	 * suggested until required ones are all defined)
	 */
	boolean tieredOptionalPropertyProposals();
	/**
	 * Config option for type-based completion engine. This enables/disables
	 * whether engine should generate proposals for deprecated properties (true), 
	 * or suppress them (false).
	 */
	boolean suggestDeprecatedProperties();
}
