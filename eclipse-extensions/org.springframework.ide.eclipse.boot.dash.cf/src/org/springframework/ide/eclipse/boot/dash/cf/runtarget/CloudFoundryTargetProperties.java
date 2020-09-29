/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.runtarget;

import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.StoreCredentialsMode;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;

import com.google.gson.Gson;

public class CloudFoundryTargetProperties extends TargetProperties {

	public final static String ORG_PROP = "organization";
	public final static String SPACE_PROP = "space";
	public final static String SELF_SIGNED_PROP = "selfsigned";
	public final static String SKIP_SSL_VALIDATION_PROP = "skipSslValidation";

	public final static String ORG_GUID = "organization_guid";
	public final static String SPACE_GUID = "space_guid";

	public static final String USERNAME_PROP = "username";
	public static final String URL_PROP = "url";
	private static final String STORE_CREDENTIALS = "storeCredentials";

	private CFCredentials credentials;

	final private SimpleDIContext injections;

	public CFCredentials getCredentials() throws CannotAccessPropertyException {
		if (credentials == null) {
			StoreCredentialsMode storeMode = getStoreCredentials();
			try {
				credentials = storeMode.loadCredentials(context(), type, getRunTargetId());
			} catch (Exception e) {
				throw new CannotAccessPropertyException("Cannot read password.", e);
			}
		}
		return credentials;
	}

	public void setCredentials(CFCredentials credentials) throws CannotAccessPropertyException {
		this.credentials = credentials;
		StoreCredentialsMode storeMode = getStoreCredentials();
		storeMode.saveCredentials(context(), type, getRunTargetId(), credentials);
	}

	private BootDashModelContext context() {
		return injections.getBean(BootDashModelContext.class);
	}

	public CloudFoundryTargetProperties(TargetProperties copyFrom, RunTargetType<CloudFoundryTargetProperties> runTargetType, SimpleDIContext injections) {
		super(copyFrom, runTargetType);
		this.injections = injections;
		if (get(RUN_TARGET_ID) == null) {
			put(RUN_TARGET_ID, getId(this));
		}
	}

	public String getSpaceName() {
		return map.get(SPACE_PROP);
	}

	public String getOrganizationName() {
		return map.get(ORG_PROP);
	}

	public String getSpaceGuid() {
		return map.get(SPACE_GUID);
	}

	public String getOrganizationGuid() {
		return map.get(ORG_GUID);
	}

	public boolean isSelfsigned() {
		return map.get(SELF_SIGNED_PROP) != null && Boolean.parseBoolean(map.get(SELF_SIGNED_PROP));
	}

	public boolean skipSslValidation() {
		return map.get(SKIP_SSL_VALIDATION_PROP) != null && Boolean.parseBoolean(map.get(SKIP_SSL_VALIDATION_PROP));
	}

	public static String getId(CloudFoundryTargetProperties cloudProps) {
		return getId(cloudProps.getUsername(), cloudProps.getUrl(), cloudProps.getOrganizationName(),
				cloudProps.getSpaceName());
	}

	public static String getName(CloudFoundryTargetProperties cloudProps) {
		return cloudProps.getOrganizationName() + " : " + cloudProps.getSpaceName() + " - [" + cloudProps.getUrl()
				+ "]";
	}

	public static String getId(CFClientParams params) {
		return getId(params.getUsername(), params.getApiUrl(), params.getOrgName(), params.getSpaceName());
	}

	public static String getId(String userName, String url, String orgName, String spaceName) {
		return userName + " : " + url + " : " + orgName + " : " + spaceName;
	}

	public boolean isStoreCredentials() {
		return getStoreCredentials()!=StoreCredentialsMode.STORE_NOTHING;
	}

	public void setSpace(CFSpace space) {
		if (space != null) {
			put(ORG_PROP, space.getOrganization().getName());
			put(ORG_GUID, space.getOrganization().getGuid().toString());
			put(SPACE_PROP, space.getName());
			put(SPACE_GUID, space.getGuid().toString());
		} else {
			put(ORG_PROP, null);
			put(ORG_GUID, null);
			put(SPACE_PROP, null);
			put(SPACE_GUID, null);
		}
	}

	public void setUrl(String value) {
		put(URL_PROP, value);
	}

	public void setSelfSigned(boolean value) {
		put(SELF_SIGNED_PROP, Boolean.toString(value));
	}

	public void setSkipSslValidation(boolean value) {
		put(SKIP_SSL_VALIDATION_PROP, Boolean.toString(value));
	}

	public String getUsername() {
		return map.get(USERNAME_PROP);
	}
	public void setUserName(String value) {
		put(USERNAME_PROP, value);
	}

	public StoreCredentialsMode getStoreCredentials() {
		String s = map.get(STORE_CREDENTIALS);
		if (s!=null) {
			return StoreCredentialsMode.valueOf(s);
		} else {
			return StoreCredentialsMode.STORE_NOTHING;
		}
	}

	public void setStoreCredentials(StoreCredentialsMode store) {
		map.put(STORE_CREDENTIALS, String.valueOf(store));
	}

	public String getUrl() {
		return map.get(URL_PROP);
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
