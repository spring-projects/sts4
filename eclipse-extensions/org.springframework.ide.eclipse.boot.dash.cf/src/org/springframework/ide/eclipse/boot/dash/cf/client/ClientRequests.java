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
package org.springframework.ide.eclipse.boot.dash.cf.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientRequests extends Disposable {

	/**
	 * The actual Rest API version that cloud controller claims to be.
	 */
	Version getApiVersion() throws Exception;

	/**
	 * The minimum version that the CF V2 java client claims to support.
	 */
	Version getSupportedApiVersion();

	/**
	 * Returns null if the application does not exist. Throws some kind of Exception if there's any other kind of problem.
	 */
	CFApplicationDetail getApplication(String appName) throws Exception;

	//TODO: consider removing the getXXXSupport method and directly adding the apis that these support
	// objects provide.
	SshClientSupport getSshClientSupport() throws Exception;


	void deleteApplication(String name) throws Exception;

	List<CFApplication> getApplicationsWithBasicInfo() throws Exception;
	List<CFBuildpack> getBuildpacks() throws Exception;
	List<CFCloudDomain> getDomains() throws Exception;
	List<CFServiceInstance> getServices() throws Exception;
	List<CFSpace> getSpaces() throws Exception;
	List<CFStack> getStacks() throws Exception;
	void restartApplication(String appName, CancelationToken token) throws Exception;
	void stopApplication(String appName) throws Exception;
	reactor.core.Disposable streamLogs(String appName, IApplicationLogConsole logConsole) throws Exception;
	Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception;
	String getHealthCheck(UUID appGuid) throws Exception;
	void setHealthCheck(UUID guid, String hcType) throws Exception;
	boolean applicationExists(String appName) throws Exception;

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

	/**
	 * The returned flux will provide the current token in its onNext immediately, if there is a current
	 * token already. Subsequent onNext will be fired whenever the token changes.
	 */
	Flux<String> getRefreshTokens();
}
