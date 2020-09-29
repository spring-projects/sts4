/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.internal.commands;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Base implementation of a parameter factory that descriptors adapt to. Clients
 * can implement their own adapter factory and contribute it through platform
 * adapter manager. Users should not be instantiating this class directly, as
 * the adapter framework will manage factory lookup.
 * <p>
 * A static method is provided that users can call to obtain a parameter via the
 * adapter manager framework.
 * </p>
 * @author Nieraj Singh
 */
public class ParameterFactory implements IParameterFactory {

	/**
	 * Users should not instantiate this class or invoke this method directly.
	 * Use the static method instead, as it leverages the adapter manager
	 * framework.
	 */
	public ICommandParameter getParameter(ICommandParameterDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}

		ParameterKind kind = descriptor.getParameterKind();
		switch (kind) {
		case BASE:
		case JAVA_TYPE:
			return new StringCommandParameter(descriptor);
		case BOOLEAN:
		case COMBO:
			return new CommandParameter(descriptor);
		case COMPOSITE:
			return new CompositeCommandParameter(
					(CompositeParameterDescriptor) descriptor);
		}
		return null;
	}

	/**
	 * A composite parameter is a parameter whose single value can be obtained
	 * by different types of parameter descriptors. However, a composite value
	 * always has just one value, just like any other parameter.
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           optional. If not required, use null.
	 * @param requiresName
	 *           true if the name of the parameter should appear in the Command
	 *           string
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 * @param valueSeparator
	 *           non-null if the value of the parameter requires a value
	 *           separator like "=". Null otherwise
	 * @param descriptors
	 *           list of descriptors that define this composite.
	 * @return
	 */
	public static ICommandParameterDescriptor createCompositeParameterDescriptor(
			String name, String description, boolean isMandatory,
			String defaultValue, boolean requiresName, String delimiter,
			String valueSeparator, List<ICommandParameterDescriptor> descriptors) {
		return new CompositeParameterDescriptor(name, description, isMandatory,
				defaultValue, requiresName, delimiter, valueSeparator,
				descriptors);
	}

	/**
	 * 
	 * Use this constructor if the boolean value is either "true" or "false",
	 * meaning that the parameter has a pattern:
	 * <p>
	 * [parametername]=true
	 * </p>
	 * <p>
	 * [parametername]=false
	 * </p>
	 * Boolean parameters ALWAYS have a default value (true or false)
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . True or false
	 * @param requiresName
	 *           true if the name of the parameter should appear in the Command
	 *           string
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 * @param valueSeparator
	 *           non-null if the value of the parameter requires a value
	 *           separator like "=". Null otherwise
	 */
	public static ICommandParameterDescriptor createBooleanParameterDescriptor(
			String name, String description, boolean isMandatory,
			boolean defaultValue, boolean requiresName, String prefix,
			String valueSeparator) {
		return new BooleanParameterDescriptor(name, description, isMandatory,
				defaultValue, requiresName, prefix, valueSeparator);
	}

	/**
	 * User this constructor if the name of the parameter itself is also the
	 * value. This should be used when the parameter either appears in the
	 * command string with its name if set to true, or is omitted altogether if
	 * the value is false
	 * 
	 * <p>
	 * 
	 * parameter name: "windows". If true, it appears as: "--windows" in the
	 * command string. If false, it is omitted. The "--" needs to be defined as
	 * the prefix
	 * </p>
	 * Boolean parameters ALWAYS have a default value (true or false)
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . True or false
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 */
	public static ICommandParameterDescriptor createBooleanParameterDescriptor(
			String name, String description, boolean isMandatory,
			boolean defaultValue, String prefix) {
		return new BooleanParameterDescriptor(name, description, isMandatory,
				defaultValue, prefix);
	}

	/**
	 * Basic text-based parameter.
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . Null if it has no default value
	 * @param requiresName
	 *           true if the name of the parameter should appear in the Command
	 *           string
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 * @param valueSeparator
	 *           non-null if the value of the parameter requires a value
	 *           separator like "=". Null otherwise
	 * @return
	 */
	public static ICommandParameterDescriptor createBaseParameterDescriptor(
			String name, String description, boolean isMandatory,
			String defaultValue, boolean requiresName, String prefix,
			String valueSeparator) {
		return new ParameterDescriptor(name, description, isMandatory,
				defaultValue, ParameterKind.BASE, requiresName, prefix,
				valueSeparator);
	}

	/**
	 * Creates a Java parameter that pertains to a Java type. The specified Java
	 * type allows UI components to support the correct type of content assist,
	 * type browsing and filtering
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . Null if it has no default value
	 * @param requiresName
	 *           true if the name of the parameter should appear in the Command
	 *           string
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 * @param valueSeparator
	 *           non-null if the value of the parameter requires a value
	 *           separator like "=". Null otherwise
	 * @param type
	 *           must be a valid Java type that this parameter corresponds to.
	 *           This may be used for content assist and Java type browsing.
	 *           See valid values: {@link JavaParameterDescriptor}
	 * @return
	 */
	public static ICommandParameterDescriptor createJavaParameterDescriptor(
			String name, String description, boolean isMandatory,
			String defaultValue, boolean requiresName, String prefix,
			String valueSeparator, int type) {
		return new JavaParameterDescriptor(name, description, isMandatory,
				defaultValue, ParameterKind.JAVA_TYPE, type, requiresName,
				prefix, valueSeparator);
	}

	/**
	 * Creates a combo descriptor for the given set of values.
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . Null if it has no default value
	 * @param requiresName
	 *           true if the name of the parameter should appear in the Command
	 *           string
	 * @param prefix
	 *           non-null if a prefix like "--" should be prepended to the
	 *           parameter name. Null otherwise
	 * @param valueSeparator
	 *           non-null if the value of the parameter requires a value
	 *           separator like "=". Null otherwise
	 * @param values
	 *           list of values that appear in the Combo. Cannot be null or
	 *           empty.
	 * @return
	 */
	public static ICommandParameterDescriptor createComboParameterDescriptor(
			String name, String description, boolean isMandatory,
			String defaultValue, boolean requiresName, String prefix,
			String valueSeparator, String[] values) {
		return new ComboParameterDescriptor(name, description, isMandatory,
				defaultValue, requiresName, prefix, valueSeparator, values);
	}

	/**
	 * Creates a parameter descriptor that follows this pattern:
	 * "--[parametername]=[value]".
	 * 
	 * <p>
	 * If the parameter is serialised, it is prefixed by "--", followed by the
	 * exact parameter name, followed by "=" followed by the value.
	 * </p>
	 * 
	 * @param name
	 *           of the command exactly as it should appear when executed.
	 *           Cannot be null or empty.
	 * @param description
	 *           optional description
	 * @param isMandatory
	 *           true if it is required. False otherwise
	 * @param defaultValue
	 *           . Null if it has no default value
	 * @return new Command descriptor for the given arguments.
	 */
	public static ICommandParameterDescriptor createBasePrefixedParameterDescriptor(
			String name, String description, boolean isMandatory,
			String defaultValue) {
		return createBaseParameterDescriptor(name, description, isMandatory,
				defaultValue, true, "--", "=");
	}

	/**
	 * Users should use this method to obtain a parameter instance for a given
	 * parameter descriptor
	 * 
	 * @param descriptor
	 * @return new parameter instance for the given parameter descriptor, or
	 *        null if it couldn't be created
	 */
	public static ICommandParameter getParameterInstance(
			ICommandParameterDescriptor descriptor) {
		if (descriptor instanceof IAdaptable) {
			IParameterFactory factory = (IParameterFactory) ((IAdaptable) descriptor)
					.getAdapter(IParameterFactory.class);
			if (factory != null) {
				return factory.getParameter(descriptor);
			}
		}
		return null;
	}

}
