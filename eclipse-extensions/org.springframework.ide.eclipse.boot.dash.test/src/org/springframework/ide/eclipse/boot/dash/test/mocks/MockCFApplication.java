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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFInstanceState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cf.client.InstanceState;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.ApplicationExtras;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFApplicationDetailData;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFApplicationSummaryData;
import org.springframework.ide.eclipse.boot.dash.cf.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import reactor.core.publisher.Mono;

public class MockCFApplication {

	private static class MockCFInstanceStats implements CFInstanceStats {

		private CFInstanceState state;

		public MockCFInstanceStats(CFInstanceState state) {
			this.state = state;
		}

		@Override
		public CFInstanceState getState() {
			return state;
		}

		@Override
		public String toString() {
			return "CFInstanceState("+state+")";
		}
	}

	private final String name;
	private final UUID guid;
	private int instances;

	private Map<String, String> env = new HashMap<>();
	private int memory = 1024;
	private List<String> services = new ArrayList<>();
	private String buildpackUrl = null;
	private List<RouteBinding> routes = new ArrayList<>();
	private CFAppState state = CFAppState.STOPPED;
	private int diskQuota = 1024;
	private Integer timeout = null;
	private String healthCheckType = DeploymentProperties.DEFAULT_HEALTH_CHECK_TYPE;
	private String healthCheckHttpEndpoint = null;
	private String command = null;
	private String stack = null;
	private MockCloudFoundryClientFactory owner;
	private MockCFSpace space;

	public MockCFApplication(MockCloudFoundryClientFactory owner, MockCFSpace space, String name, UUID guid, int instances, CFAppState state) {
		this.owner = owner;
		this.space = space;
		this.name = name;
		this.guid = guid;
		this.instances = instances;
		this.state = state;
		this.cancelationTokens = new CancelationTokens();
	}

	private ImmutableList<CFInstanceStats> stats = ImmutableList.of();

	private CancelationTokens cancelationTokens;
	private Supplier<byte[]> bits;

	public MockCFApplication(MockCloudFoundryClientFactory owner,  MockCFSpace space, String name) {
		this(owner,
				space,
				name,
				UUID.randomUUID(),
				1,
				CFAppState.STOPPED
		);
	}

	public String getName() {
		return name;
	}

	public List<CFInstanceStats> getStats() {
		return stats;
	}

	public void start(CancelationToken cancelationToken) throws Exception {
		Assert.isLegal(CFAppState.STOPPED==state);
		Assert.isLegal(stats.isEmpty());
		this.state = CFAppState.UNKNOWN;
		final long endTime = System.currentTimeMillis()+getStartDelay();
		new ACondition("simulated app starting (waiting)", getStartDelay()+1000) {
			@Override
			public boolean test() throws Exception {
//				System.out.println("Checking token: "+cancelToken);
				if (!cancelationToken.isCanceled() && System.currentTimeMillis()<endTime) {
					System.out.println("Starting "+getName()+"...");
					throw new IOException("App still starting");
				}
				return true;
			}
		};
		Builder<CFInstanceStats> builder = ImmutableList.builder();
		for (int i = 0; i < instances; i++) {
			Map<String, Object> values = new HashMap<>();
			values.put("state", InstanceState.RUNNING.toString());
			CFInstanceStats stat = new MockCFInstanceStats(CFInstanceState.RUNNING);
			builder.add(stat);
		}
		if (cancelationToken.isCanceled()) {
			System.out.println("Starting "+getName()+" CANCELED");
			throw new IOException("Operation Canceled");
		}
		this.stats = builder.build();
		this.state = CFAppState.STARTED;
		System.out.println("Starting "+getName()+" SUCCESS");
	}

	private long getStartDelay() {
		return owner.getStartDelay();
	}

	public String getHealthCheckType() {
		return healthCheckType;
	}

	public void setHealthCheckType(String t) {
		this.healthCheckType = t;
	}

	public String getHealthCheckHttpEndpoint() {
		return healthCheckHttpEndpoint;
	}

	public void setHealthCheckHttpEndpoint(String t) {
		this.healthCheckHttpEndpoint = t;
	}

	public void setHealthCheckTypeMaybe(String t) {
		if (t!=null) {
			setHealthCheckType(t);
		}
	}


//	public int getInstances() {
//		return instances;
//	}

	public int getRunningInstances() {
		int runningInstances = 0;
		for (CFInstanceStats instance : getStats()) {
			if (instance.getState()==CFInstanceState.RUNNING) {
				runningInstances++;
			}
		}
		return runningInstances;
	}

	public UUID getGuid() {
		return guid;
	}

	public void setBuildpackUrl(String buildpackUrl) {
		this.buildpackUrl = buildpackUrl;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	public void setDiskQuota(int diskQuota) {
		this.diskQuota = diskQuota;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getMemory() {
		return this.memory;
	}

	public void setRoutes(Collection<RouteBinding> routes) {
		this.routes = routes==null?null:ImmutableList.copyOf(routes);
	}

	public void setServices(Collection<String> services) {
		this.services = services==null?null:ImmutableList.copyOf(services);
	}

	public CFApplication getBasicInfo() {
		return new CFApplicationSummaryData(
				name,
				instances,
				getRunningInstances(),
				memory,
				guid,
				getUris(),
				state,
				diskQuota,
				getExtras()
		);
	}

	private List<String> getUris() {
		return routes.stream()
		.map(RouteBinding::toUri)
		.collect(Collectors.toList());
	}

	private ApplicationExtras getExtras() {
		return new ApplicationExtras() {
			@Override
			public Mono<String> getStack() {
				return Mono.justOrEmpty(stack);
			}
			@Override
			public Mono<List<String>> getServices() {
				return Mono.justOrEmpty(services);
			}

			@Override
			public Mono<Map<String, String>> getEnv() {
				return Mono.justOrEmpty(env);
			}

			@Override
			public Mono<String> getBuildpack() {
				return Mono.justOrEmpty(buildpackUrl);
			}
			@Override
			public Mono<Integer> getTimeout() {
				return Mono.justOrEmpty(timeout);
			}
			@Override
			public Mono<String> getCommand() {
				return Mono.justOrEmpty(command);
			}
			@Override
			public Mono<String> getHealthCheckType() {
				return Mono.justOrEmpty(healthCheckType);
			}
			@Override
			public Mono<String> getHealthCheckHttpEndpoint() {
				return Mono.justOrEmpty(healthCheckHttpEndpoint);
			}

		};
	}

	public CFApplicationDetail getDetailedInfo() {
		return new CFApplicationDetailData(
				new CFApplicationSummaryData(
						name,
						instances,
						getRunningInstances(),
						memory,
						guid,
						getUris(),
						state,
						diskQuota,
						getExtras()
				),
				ImmutableList.copyOf(stats)
		);
//		return new CFApplicationDetailData(getBasicInfo(), ImmutableList.copyOf(stats));
	}

	@Override
	public String toString() {
		return "MockCFApp("+name+")";
	}

	public void stop() {
		cancelationTokens.cancelAll();
		this.stats = ImmutableList.of();
		this.state = CFAppState.STOPPED;
	}

	public Map<String, String> getEnv() {
		return env;
	}

	public void setEnv(Map<String, String> newEnv) {
		env = ImmutableMap.copyOf(newEnv);
	}

	public void restart(CancelationToken cancelationToken) throws Exception {
		stop();
		start(cancelationToken);
	}

	public void scaleInstances(int desiredInstances) {
		Assert.isLegal(desiredInstances>0);
		Builder<CFInstanceStats> builder = ImmutableList.builder();
		builder.addAll(stats);
		for (int i = 0; i < desiredInstances; i++) {
			builder.add(new MockCFInstanceStats(CFInstanceState.RUNNING));
		}
		stats = builder.build();
		this.instances = desiredInstances;
	}


	public void setBuildpackUrlMaybe(String buildpack) {
		if (buildpack!=null) {
			setBuildpackUrl(buildpack);
		}
	}

	public void setCommandMaybe(String command) {
		if (command!=null) {
			setCommand(command);
		}
	}

	public void setDiskQuotaMaybe(Integer diskQuota) {
		if (diskQuota!=null) {
			setDiskQuota(diskQuota);
		}
	}

	public void setEnvMaybe(Map<String, String> env) {
		if (env!=null) {
			setEnv(env);
		}
	}

	public void setMemoryMaybe(Integer memory) {
		if (memory!=null) {
			setMemory(memory);
		}
	}

	public void setServicesMaybe(List<String> services) {
		if (services!=null) {
			setServices(services);
		}
	}

	public void setStackMaybe(String stack) {
		if (stack!=null) {
			setStack(stack);
		}
	}

	public void setTimeoutMaybe(Integer timeout) {
		if (timeout!=null) {
			setTimeout(timeout);
		}
	}

	public int getPushCount() {
		return space.getPushCount(name).getValue();
	}

	public String getFileContents(String path) throws IOException {
		ZipInputStream zip = new ZipInputStream(getBits());
		ZipEntry entry;
		while (null != (entry = zip.getNextEntry())) {
			if (!entry.isDirectory()) {
				if (entry.getName().equals(path)) {
					return new String(readBytes(zip));
				}
			}
		}
		return null;
	}

	private byte[] readBytes(ZipInputStream zip) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		int nextByte;
		while ((nextByte = zip.read())>=0) {
			bytes.write(nextByte);
		}
		return bytes.toByteArray();
	}

	public void setBits(Supplier<byte[]> bytes) {
		this.bits = bytes;
	}

	public ByteArrayInputStream getBits() {
		return new ByteArrayInputStream(bits.get());
	}



}
