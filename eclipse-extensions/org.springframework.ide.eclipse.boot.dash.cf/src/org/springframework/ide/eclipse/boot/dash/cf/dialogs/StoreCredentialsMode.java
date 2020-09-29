/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.util.EnumSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.pstore.IPropertyStore;
import org.springsource.ide.eclipse.commons.livexp.ui.Ilabelable;

public enum StoreCredentialsMode implements Ilabelable {

	STORE_PASSWORD {
		@Override
		public String getLabel() {
			return "Store Password";
		}

		@Override
		public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) throws CannotAccessPropertyException {
			try {
				String password = context.getSecuredCredentialsStore().getCredentials(secureStoreScopeKey(type.getName(), runTargetId));
				if (password!=null) {
					return CFCredentials.fromPassword(password);
				}
				return null;
			} catch (StorageException e) {
				throw new CannotAccessPropertyException("Failed to load credentials", e);
			}
		}

		@Override
		protected void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
			try {
				String storedString = credentials.getSecret();
				context.getSecuredCredentialsStore().setCredentials(secureStoreScopeKey(type.getName(), runTargetId), storedString);
			} catch (StorageException e) {
				throw new CannotAccessPropertyException("Failed to save credentials", e);
			}
		}

		@Override
		protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
			try {
				SecuredCredentialsStore store = context.getSecuredCredentialsStore();
				//Be careful and avoid annoying password popup just to erase data in a locked secure store.
				if (store.isUnlocked()) {
					store.setCredentials(secureStoreScopeKey(type.getName(), runTargetId), null);
				}
			} catch (StorageException e) {
				Log.log(e);
			}
		}

		private String secureStoreScopeKey(String targetTypeName, String targetId) {
			return targetTypeName+":"+targetId;
		}
	},

	STORE_TOKEN {
		@Override
		public String getLabel() {
			return "Store OAuth Token";
		}

		private String privateStoreKey(String targetType, String targetId) {
			return targetType+":"+targetId + ":token";
		}

		@Override
		public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
			String token = context.getPrivatePropertyStore().get(privateStoreKey(type.getName(), runTargetId));
			if (token!=null) {
				return CFCredentials.fromRefreshToken(token);
			}
			return null;
		}

		@Override
		public void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
			try {
				String storedString = credentials.getSecret();
				context.getPrivatePropertyStore().put(privateStoreKey(type.getName(), runTargetId), storedString);
			} catch (Exception e) {
				throw new CannotAccessPropertyException("Failed to save credentials", e);
			}
		}

		@Override
		protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
			try {
				IPropertyStore store = context.getPrivatePropertyStore();
				store.put(privateStoreKey(type.getName(), runTargetId), null);
			} catch (Exception e) {
				Log.log(e);
			}
		}
	},

	STORE_NOTHING {
		@Override
		public String getLabel() {
			return "Do NOT Store";
		}

		@Override
		public CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
			return null;
		}

		@Override
		protected void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) {
			//nothing to do
		}

		@Override
		protected void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) {
			//nothing to do
		}
	};

	public abstract CFCredentials loadCredentials(BootDashModelContext context, RunTargetType type, String runTargetId) throws CannotAccessPropertyException;
	protected abstract void eraseCredentials(BootDashModelContext context, RunTargetType type, String runTargetId);
	protected abstract void basicSaveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException;

	public final void saveCredentials(BootDashModelContext context, RunTargetType type, String runTargetId, CFCredentials credentials) throws CannotAccessPropertyException {
		for (StoreCredentialsMode mode : EnumSet.allOf(StoreCredentialsMode.class)) {
			if (mode==this) {
				mode.basicSaveCredentials(context, type, runTargetId, credentials);
			} else {
				mode.eraseCredentials(context, type, runTargetId);
			}
		}
	}
}