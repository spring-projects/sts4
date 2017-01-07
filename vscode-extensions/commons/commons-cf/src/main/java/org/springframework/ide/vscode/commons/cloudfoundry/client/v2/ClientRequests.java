/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplication;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFSpace;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.SshClientSupport;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CancelationTokens.CancelationToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientRequests {

//	/**
//	 * The actual Rest API version that cloud controller claims to be.
//	 */
//	Version getApiVersion();
//
//	/**
//	 * The minimum version that the CF V2 java client claims to support.
//	 */
//	Version getSupportedApiVersion();

	/**
	 * Returns null if the application does not exist. Throws some kind of Exception if there's any other kind of problem.
	 */
	CFApplicationDetail getApplication(String appName) throws Exception;

	//TODO: consider removing the getXXXSupport method and directly adding the apis that these support
	// objects provide.
	SshClientSupport getSshClientSupport() throws Exception;


	void deleteApplication(String name) throws Exception;
	void logout();

	List<CFApplication> getApplicationsWithBasicInfo() throws Exception;
	List<CFBuildpack> getBuildpacks() throws Exception;
	List<CFCloudDomain> getDomains() throws Exception;
	List<CFServiceInstance> getServices() throws Exception;
	List<CFSpace> getSpaces() throws Exception;
	List<CFStack> getStacks() throws Exception;
	void restartApplication(String appName, CancelationToken token) throws Exception;
	void stopApplication(String appName) throws Exception;
	Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception;
	String getHealthCheck(UUID appGuid) throws Exception;
	void setHealthCheck(UUID guid, String hcType) throws Exception;
	boolean applicationExists(String appName) throws Exception;

	//Removed in V2
	//void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception;

	//Added since v2:
	void push(CFPushArguments args, CancelationToken cancelationToken) throws Exception;
	Map<String, String> getApplicationEnvironment(String appName) throws Exception;
	Mono<Void> deleteServiceAsync(String serviceName);

	/**
	 * Gets current value of the client's refresh token. Note that the token is only set once it is known.
	 * Initially, if a client is created via password auth, then the refreshToken won't be known until
	 * some operation has been executed.
	 *
	 * @return Refresh token if it is already known, null otherwise.
	 */
	String getRefreshToken();
	Mono<String> getUserName();
}
