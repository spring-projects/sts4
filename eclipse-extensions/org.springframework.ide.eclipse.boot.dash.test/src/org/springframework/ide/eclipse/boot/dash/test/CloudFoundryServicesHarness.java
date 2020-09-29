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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryClientTest.FLAKY_SERVICE_BROKER;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableMap;

public class CloudFoundryServicesHarness implements Disposable {

	public static final Duration CREATE_SERVICE_TIMEOUT = Duration.ofMinutes(1);

	private static final Duration DELETE_SERVICE_TIMEOUT = Duration.ofMinutes(1);

	private DefaultClientRequestsV2 client;

	private CFClientParams clientParams;

	public CloudFoundryServicesHarness(CFClientParams clientParams, DefaultClientRequestsV2 client) {
		this.client = client;
		this.clientParams = clientParams;
	}
	private Set<String> ownedServiceNames  = new HashSet<>();

	public String[] getTestServiceAndPlan() {
		String org = clientParams.getOrgName();
		String api = clientParams.getApiUrl();
		if (org.equals("application-platform-testing")) {
			//PWS test space/org
			return new String[] {"cloudamqp", "lemur"};
		} else if (api.contains("api.run.pez.")) {
			//PEZ
			return new String[] {"p-rabbitmq", "standard"};
		} else if (api.contains("api.tan.")) {
			//TAN RGB
			//return new String[] {"app-autoscaler", "bronze"}; //Warning not good enough for some tests!
			return new String[] {"p-rabbitmq", "standard"};
		} else if (api.contains("wdc-06-pcf2-system.oc.vmware.com")) {
			return new String[] {"p-rabbitmq", "standard"};
		}
		return null;
	}

	public String randomServiceName() {
		String name = StringUtil.datestamp()+"-"+randomAlphabetic(10);
		ownedServiceNames.add(name);
		return name;
	}

	public String createTestUserProvidedService() {
		String name = randomServiceName();
		client.createUserProvidedService(name, ImmutableMap.of()).block(CREATE_SERVICE_TIMEOUT);
		return name;
	}

	public String createTestService() throws Exception {
		String name = randomServiceName();
		RetryUtil.retryWhen("createTestService["+name+"]", 5, FLAKY_SERVICE_BROKER, () -> {
			String[] serviceParams = getTestServiceAndPlan();
			client.createService(name, serviceParams[0], serviceParams[1]).block(CREATE_SERVICE_TIMEOUT);
		});
		return name;
	}

	protected void deleteOwnedServices() {
		if (!ownedServiceNames.isEmpty()) {
			System.out.println("owned services: "+ownedServiceNames);
			try {
				for (String serviceName : ownedServiceNames) {
					System.out.println("delete service: "+serviceName);
					try {
						RetryUtil.retryTimes("delete sercice "+serviceName, 3, () -> {
							this.client.deleteServiceAsync(serviceName).block(DELETE_SERVICE_TIMEOUT);
						});
					} catch (Exception e) {
						System.out.println("Failed to delete ["+serviceName+"]: "+ExceptionUtil.getMessage(e));

						// May get 404 or other 400 errors if it is alrready
						// gone so don't prevent other owned apps from being
						// deleted
					}
				}

			} catch (Exception e) {
				fail("failed to cleanup owned services: " + e.getMessage());
			}
		}
	}

	@Override
	public void dispose() {
		assertTrue(!client.isLoggedOut());
		deleteOwnedServices();
	}

}
