/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private static final Logger logger = Logger.getLogger(DefaultClientRequestsV2.class.getName());
	private static final boolean DEBUG = false;

	private CloudFoundryClient _client ;
	private CloudFoundryOperations _operations;

	private final ClientTimeouts timeouts;

	public DefaultClientRequestsV2(CloudFoundryClientCache clients, CFClientParams params, ClientTimeouts timeouts) {
		CFClientProvider provider = getFromCache(clients, params);
		this._client = provider.client;

		this._operations = DefaultCloudFoundryOperations.builder()
				.cloudFoundryClient(_client)
				.dopplerClient(provider.doppler)
				.uaaClient(provider.uaaClient)
				.organization(params.getOrgName())
				.space(params.getSpaceName())
				.build();

		// timeouts must never be null
		this.timeouts = timeouts != null ? timeouts : ClientTimeouts.DEFAULT_TIMEOUTS;
	}

	private CFClientProvider getFromCache(CloudFoundryClientCache clients, CFClientParams params) {
		CFClientProvider provider = null;
		try {
			provider = clients.getOrCreate(params);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create a v2 CF Java client using params: " + params, e);
		}
		return provider;
	}

	@Override
	public List<CFServiceInstance> getServices() throws Exception {
		return ReactorUtils.get(timeouts.getServicesTimeout(), CancelationTokens.NULL,
			log("operations.services.listIntances",
				_operations
				.services()
				.listInstances()
				.map(CFWrappingV2::wrap)
				.collectList()
				.map(ImmutableList::copyOf)
			)
		);
	}

	@Override
	public List<CFDomain> getDomains() throws Exception {

		return ReactorUtils.get(timeouts.getBuildpacksTimeout(), CancelationTokens.NULL,
				log("operations.domains.list",
					_operations
					.domains()
					.list()
					.map(CFWrappingV2::wrap)
					.collectList()
					.map(ImmutableList::copyOf)
				)
			);
	}

	@Override
	public List<CFBuildpack> getBuildpacks() throws Exception {
		return ReactorUtils.get(timeouts.getBuildpacksTimeout(), CancelationTokens.NULL,
			log("operations.buildpacks.list",
				_operations
				.buildpacks()
				.list()
				.map(CFWrappingV2::wrap)
				.collectList()
				.map(ImmutableList::copyOf)
			)
		);
	}
	@Override
	public List<CFStack> getStacks() throws Exception {
		return ReactorUtils.get(
			log("operations.stacks().list()",
				_operations.stacks()
				.list()
				.map(CFWrappingV2::wrap)
				.collectList()
			)
		);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//// calls to client and operations with 'logging'.

	private <T> Flux<T> log(String msg, Flux<T> flux) {
		if (DEBUG) {
			return flux
			.doOnSubscribe((sub) -> debug(">>> "+msg))
			.doOnComplete(() -> {
				debug("<<< "+msg+" OK");
			})
			.doOnCancel(() -> {
				debug("<<< "+msg+" CANCEL");
			})
			.doOnError((error) -> {
				debug("<<< "+msg+" ERROR: "+ExceptionUtil.getMessage(error));
			});
		} else {
			return flux;
		}
	}

	private <T> Mono<T> log(String msg, Mono<T> mono) {
		if (DEBUG) {
			return mono
			.doOnSubscribe((sub) -> debug(">>> "+msg))
			.doOnCancel(() -> debug("<<< "+msg+" CANCEL"))
			.doOnSuccess((data) -> {
				debug("<<< "+msg+" OK");
			})
			.doOnError((error) -> {
				debug("<<< "+msg+" ERROR: "+ExceptionUtil.getMessage(error));
			});
		} else {
			return mono;
		}
	}

	private void debug(String msg) {
		logger.log(Level.INFO, msg);
	}
}
