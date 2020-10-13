/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.model.DeletionCapabableModel;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.util.template.Templates;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.core.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

/**
 * A run target type represents a type of 'deployment environment' to which
 * boot apps can be targetted to run. For example 'local', 'cloudfoundry'
 * or 'lattice'.
 *
 * @author Kris De Volder
 */
public interface RunTargetType<Params> extends Nameable {

	/**
	 * @return Whether it is possible to create instances of this type. Not all
	 * runtargets provide this ability. For example the 'local' run target
	 * is a singleton and doesn't allow creating instances.
	 */
	boolean canInstantiate();

	/**
	 * RunTargetTypes that return 'true' from 'canCreate' must provide an implementation
	 * of this method. When called it opens a UI allowing the user to create a new
	 * run target.
	 */
	CompletableFuture<?> openTargetCreationUi(LiveSetVariable<RunTarget> targets);

	/**
	 *
	 * @return if {@link #canInstantiate()} returns true, return a new {@link RunTarget}. Return null if this
	 * type cannot be instantiated.
	 */
	RunTarget<Params> createRunTarget(Params properties);

	ImageDescriptor getIcon();

	/**
	 * Provides a means to store persistent properties associated with this {@link RunTargetType}
	 */
	IPropertyStore getPropertyStore();

	/**
	 * A convenience method that provides access to the persisent property store returned by getPropertyStore
	 * through more convenient API.
	 */
	PropertyStoreApi getPersistentProperties();

	/**
	 * Provides a Default template (see {@link Templates}) for rendering the name of targets of this type.
	 * This default can be overriden via a persisent property set on this target. (I.e. the default is
	 * only used if the property isn't set.
	 */
	String getDefaultNameTemplate();

	/**
	 * Sets a persistent property that overrides the default name template.
	 */
	void setNameTemplate(String string) throws Exception;

	/**
	 * Gets the effective name template. May return null if there is neither a defaultNameTemplate nor
	 * a template provided through setNameTemplate.
	 */
	String getNameTemplate();

	/**
	 * Gets a short, helpful message describing the supported template language (i.e. at least list the
	 * supported '%' template variables.
	 */
	String getTemplateHelpText();

	Params parseParams(String serializedTargetParams);
	String serialize(Params serializedTargetParams);

	ImageDescriptor getDisconnectedIcon();

	default MissingLiveInfoMessages getMissingLiveInfoMessages() {
		return MissingLiveInfoMessages.DEFAULT;
	}

	/**
	 * Must return true if the models of this type are {@link DeletionCapabableModel}s.
	 */
	default boolean supportsDeletion() {
		return true; // true for most models, so we make this the default implementation
	}
}
