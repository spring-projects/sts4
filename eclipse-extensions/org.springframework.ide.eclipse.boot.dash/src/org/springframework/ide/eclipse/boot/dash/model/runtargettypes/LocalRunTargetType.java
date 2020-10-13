/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

/**
 * @author Kris De Volder
 */
public class LocalRunTargetType extends AbstractRunTargetType<Void> {
	LocalRunTargetType(String name) {
		super(null, name);
	}

	@Override
	public boolean canInstantiate() {
		return false;
	}

	public String toString() {
		return "RunTargetType(LOCAL)";
	}

	@Override
	public CompletableFuture<?> openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		throw new UnsupportedOperationException(
				this + " is a Singleton, it is not possible to create additional targets of this type.");
	}

	@Override
	public RunTarget<Void> createRunTarget(Void nothing) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImageDescriptor getIcon() {
		return BootDashActivator.getDefault().getImageRegistry().getDescriptor(BootDashActivator.BOOT_ICON);
	}

	@Override
	public Void parseParams(String serializedTargetParams) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String serialize(Void serializedTargetParams) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImageDescriptor getDisconnectedIcon() {
		return getIcon();
	}

	@Override
	public boolean supportsDeletion() {
		//Not supported right now.
		//It might be possible to support deletion in the future (deleting a local app could amount
		// to deleting corresponding project from the workspace, for example).
		return false;
	}
}