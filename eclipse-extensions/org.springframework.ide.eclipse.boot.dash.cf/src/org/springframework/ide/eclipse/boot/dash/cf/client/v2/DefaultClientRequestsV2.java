/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.client.v2;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.RandomStringUtils;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v2.applications.UpdateApplicationResponse;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksResponse;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.cloudfoundry.client.v2.info.GetInfoResponse;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.stacks.GetStackRequest;
import org.cloudfoundry.client.v2.stacks.GetStackResponse;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.DeleteUserProvidedServiceInstanceRequest;
import org.cloudfoundry.client.v2.users.GetUserRequest;
import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.doppler.MessageType;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationHealthCheck;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.operations.applications.PushApplicationManifestRequest;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.cloudfoundry.operations.domains.Domain;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.routes.Level;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.ServiceInstanceSummary;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.util.PaginationUtils;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CloudFoundryClientCache.CFClientProvider;
import org.springframework.ide.eclipse.boot.dash.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.console.LogType;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private static AtomicLong instances = new AtomicLong(0);

	public static final Duration APP_START_TIMEOUT = Duration.ofMinutes(10);
	public static final Duration GET_SERVICES_TIMEOUT = Duration.ofSeconds(60);
	public static final Duration GET_SPACES_TIMEOUT = Duration.ofSeconds(20);
	public static final Duration GET_USERNAME_TIMEOUT = Duration.ofSeconds(5);
	public static final Duration GET_SMALL_INFO_TIMEOUT = Duration.ofSeconds(20);

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder") || (""+Platform.getLocation()).contains("bamboo");
//	private static final boolean DEBUG_REACTOR = (""+Platform.getLocation()).contains("kdvolder");
									//|| (""+Platform.getLocation()).contains("bamboo");


	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

//	static {
//		if (DEBUG_REACTOR) {
//			Loggers.enableExtension(new Extension() {
//				@Override
//				public void log(String category, java.util.logging.Level level, String msg, Object... arguments) {
//					debug(category +"["+level + "] : "+MessageFormatter.format(msg, arguments).getMessage());
//				}
//			});
//		}
//	}


	private CFClientParams params;
	private CloudFoundryClient _client ;
	private UaaClient _uaa;
	private CloudFoundryOperations _operations;

	private Mono<String> orgId;
	private Mono<GetInfoResponse> info;
	private AbstractUaaTokenProvider _tokenProvider;
	private DefaultConnectionContext _connection;
	private String refreshToken = null;
	private Flux<String> refreshTokensFlux;

	private CompletableFuture<Boolean> _disposed = new CompletableFuture<>();

	public DefaultClientRequestsV2(CloudFoundryClientCache clients, CFClientParams params) {
		this.params = params;
		CFClientProvider provider = clients.getOrCreate(params.getUsername(), params.getCredentials(), params.getHost(), params.skipSslValidation());
		this._client = provider.client;
		this._uaa = provider.uaaClient;
		this._tokenProvider = (AbstractUaaTokenProvider) provider.tokenProvider;
		this._connection = provider.connection;
		refreshTokensFlux = _tokenProvider.getRefreshTokens(_connection).takeUntilOther(Mono.fromFuture(_disposed));
		refreshTokensFlux.doOnNext((t) -> {
			this.refreshToken = t;
		}).subscribe();

		debug(">>> creating cf operations");
		this._operations = DefaultCloudFoundryOperations.builder()
				.cloudFoundryClient(_client)
				.dopplerClient(provider.doppler)
				.uaaClient(provider.uaaClient)
				.organization(params.getOrgName())
				.space(params.getSpaceName())
				.build();
		debug("<<< creating cf operations");
		this.orgId = getOrgId();
		// Use cached info, workaround for https://www.pivotaltracker.com/story/show/158741609
		this.info = provider.info;
		debug("DefaultClientRequestsV2 created: "+instances.incrementAndGet());
	}

	private Mono<CloudFoundryOperations> client_createOperations(OrganizationSummary org) {
		return log("client.createOperations(org="+org.getName()+")",
			Mono.fromCallable(() -> DefaultCloudFoundryOperations.builder()
				.cloudFoundryClient(_client)
				.organization(org.getName())
				.build()
			)
		);
	}

	private Mono<String> getOrgId() {
		String orgName = params.getOrgName();
		if (orgName==null) {
			return Mono.error(new IOException("No organization targetted"));
		} else {
			return operations_getOrgId().cache();
		}
	}

	@Override
	public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
		return ReactorUtils.get(operations_listApps());
	}

	private ApplicationExtras getApplicationExtras(String appName) {
		//Stuff used in computing the 'extras'...
		Mono<UUID> appIdMono = getApplicationId(appName);
		Mono<ApplicationEntity> entity = appIdMono
			.flatMap((appId) ->
				client_getApplication(appId)
			)
			.map((appResource) -> appResource.getEntity())
			.cache();

		//The stuff returned from the getters of 'extras'...
		Mono<List<String>> services = prefetch("services", getBoundServicesList(appName));
		Mono<Map<String, String>> env = prefetch("env",
				DefaultClientRequestsV2.this.getEnv(appName)
		);
		Mono<String> buildpack = prefetch("buildpack",
				entity.flatMap((e) -> Mono.justOrEmpty(e.getBuildpack()))
		);

		Mono<String> stack = prefetch("stack",
			entity.flatMap((e) -> Mono.justOrEmpty(e.getStackId()))
			.flatMap((stackId) -> {
				return client_getStack(stackId);
			}).map((response) -> {
				return response.getEntity().getName();
			})
		);
		Mono<Integer> timeout = prefetch("timeout",
				entity
				.flatMap((v) -> Mono.justOrEmpty(v.getHealthCheckTimeout()))
		);

		Mono<String> command = prefetch("command",
				entity.flatMap((e) -> Mono.justOrEmpty(e.getCommand()))
		);

		Mono<String> healthCheckType = prefetch("healthCheckType",
				entity.flatMap((e) -> Mono.justOrEmpty(e.getHealthCheckType()))
		);

		Mono<String> healthCheckHttpEndpoint = prefetch("healthCheckHttpEndpoint",
				entity.flatMap((e) -> Mono.justOrEmpty(e.getHealthCheckHttpEndpoint()))
		);

		return new ApplicationExtras() {
			@Override
			public Mono<List<String>> getServices() {
				return services;
			}

			@Override
			public Mono<Map<String, String>> getEnv() {
				return env;
			}

			@Override
			public Mono<String> getBuildpack() {
				return buildpack;
			}

			@Override
			public Mono<String> getStack() {
				return stack;
			}

			@Override
			public Mono<Integer> getTimeout() {
				return timeout;
			}

			@Override
			public Mono<String> getCommand() {
				return command;
			}

			@Override
			public Mono<String> getHealthCheckType() {
				return healthCheckType;
			}

			@Override
			public Mono<String> getHealthCheckHttpEndpoint() {
				return healthCheckHttpEndpoint;
			}
		};
	}

	private <T> Mono<T> prefetch(String id, Mono<T> toFetch) {
		return toFetch
//		.log(id + " before error handler")
		.onErrorResume((error) -> {
			Log.log(new IOException("Failed prefetch '"+id+"'", error));
			return Mono.empty();
		})
//		.log(id + " after error handler")
		.cache()
//		.log(id + "after cache")
		;
	}

//	private <T> Mono<T> prefetch(Mono<T> toFetch) {
//		Mono<T> result = toFetch
//		.cache(); // It should only be fetched once.
//
//		//We must ensure the 'result' is being consumed by something to force its execution:
//		result
//		.publishOn(SCHEDULER_GROUP) //Ensure the consume is truly async or it may block here.
//		.consume((dont_care) -> {});
//
//		return result;
//	}

	@Override
	public List<CFServiceInstance> getServices() throws Exception {
		return ReactorUtils.get(GET_SERVICES_TIMEOUT, CancelationTokens.NULL,
			log("operations.services.listInstances()",
				_operations
				.services()
				.listInstances()
				.flatMap(this::getServiceDetails)
				.map(CFWrappingV2::wrap)
				.collectList()
				.map(ImmutableList::copyOf)
			)
		);
	}


	private Mono<ServiceInstance> getServiceDetails(ServiceInstanceSummary summary) {
		return log("operations.service.getServiceInstance",
				_operations.services().getInstance(GetServiceInstanceRequest.builder()
						.name(summary.getName())
						.build()
				)
		);
	}
	/**
	 * Get details for a given list of applications. This does a 'best' effort getting the details for
	 * as many apps as possible but it does not guarantee that it will return details for each app in the
	 * list. This is to avoid one 'bad apple' from spoiling the whole batch. (I.e if failing to fetch details for
	 * some apps we can still return details for the others rather than throw an exception).
	 */
	@Override
	public Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception {
		return Flux.fromIterable(appsToLookUp)
		.flatMap((CFApplication appSummary) -> {
			return getApplicationDetail(appSummary.getName())
			.onErrorResume((error) -> {
				Log.log(ExceptionUtil.coreException("getting application details for '"+appSummary.getName()+"' failed", error));
				return Mono.empty();
			})
			.map((ApplicationDetail appDetails) -> CFWrappingV2.wrap((CFApplicationSummaryData)appSummary, appDetails));
		});
	}

	@Override
	public Disposable streamLogs(String appName, IApplicationLogConsole logConsole) throws Exception {
		Flux<LogMessage> stream = log("operations.applications.logs()",
			_operations.applications()
			.logs(LogsRequest.builder()
				.name(appName)
				// BUG: show recent appears to throw exception with PWS. May be fixed in the future, but now only "pure" streaming is supported
				.recent(false)
				.build()
			)
		)
		.retryWhen(retryInterval(Duration.ofMillis(500), Duration.ofMinutes(1)))
		;

		 Disposable cancellation = ReactorUtils.sort(
				stream,
				(m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()),
				Duration.ofSeconds(1)
		)
		.map(this::convertMessageFromDoppler)
		.subscribe(logConsole::onMessage, logConsole::onError);

		return cancellation;
	}


	private org.springframework.ide.eclipse.boot.dash.console.LogMessage convertMessageFromDoppler(LogMessage msg) {
		return new org.springframework.ide.eclipse.boot.dash.console.LogMessage(
				convertMessageTypeFromDoppler(msg.getMessageType()),
				msg.getMessage()
		);
	}

	private LogType convertMessageTypeFromDoppler(MessageType mt) {
		switch (mt) {
		case OUT:
			return LogType.APP_OUT;
		case ERR:
			return LogType.APP_ERROR;
		default:
			return LogType.APP_OUT;
		}
	}
	/**
	 * Creates a retry 'signal factory' to be used with Flux.retryWhen.
	 * <p>
	 * @param timeBetween How much time to wait before retrying after a failure
	 * @param duration If this much time has elapsed when the error happens there will be no further retries.
	 * @return Functon that can be passed to retryWhen.
	 */
	private Function<Flux<Throwable>, Flux<Long>> retryInterval(Duration timeBetween, Duration duration) {
		Predicate<Throwable> falseAfterDuration = falseAfter(duration);
		return (errors) -> {
			return errors.flatMap((error) -> {
				if (falseAfterDuration.test(error)) {
					return Mono.delay(timeBetween);
				} else {
					return Mono.error(error);
				}
			});
		};
	}

	private Predicate<Throwable> falseAfter(Duration timeToWait) {
		return new Predicate<Throwable>() {

			private Long firstCalledAt;

			@Override
			public boolean test(Throwable t) {
				if (firstCalledAt==null) {
					firstCalledAt = System.currentTimeMillis();
				}
				long waitedTime = System.currentTimeMillis() - firstCalledAt;
				//debug("falseAfter: remaining = "+(timeToWait.toMillis() - waitedTime));
				return waitedTime < timeToWait.toMillis();
			}

		};
	}

	@Override
	public void stopApplication(String appName) throws Exception {
		ReactorUtils.get(
			stopApp(appName)
		);
	}

	private Mono<Void> stopApp(String appName) {
		return log("operations.applications.stop(name="+appName+")",
			_operations.applications().stop(StopApplicationRequest.builder()
				.name(appName)
				.build()
			)
		);
	}

	@Override
	public void restartApplication(String appName, CancelationToken cancelationToken) throws Exception {
		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
			restartApp(appName)
		);
	}

	private Mono<Void> restartApp(String appName) {
		return log("operations.applications().restart(name="+appName+")",
			_operations.applications().restart(RestartApplicationRequest.builder()
				.name(appName)
				.build())
		);
	}

	@Override
	public void dispose() {
		synchronized (this) {
			if (_client!=null) {
				_client = null;
				_connection = null;
				_operations = null;
				_disposed.complete(true);
				debug("DefaultClientRequestsV2.logout: "+instances.decrementAndGet());
			}
		}
	}

	public boolean isLoggedOut() {
		return _client==null;
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

	@Override
	public SshClientSupport getSshClientSupport() throws Exception {
		return new SshClientSupport() {

			@Override
			public String getSshUser(UUID appGuid, int instance) {
				return "cf:"+appGuid+"/" + instance;
			}

			@Override
			public String getSshUser(String appName, int instance) throws Exception {
				return ReactorUtils.get(
						getApplicationId(appName)
						.map((guid) -> getSshUser(guid, instance))
				);
			}

			@Override
			public SshHost getSshHost() throws Exception {
				return ReactorUtils.get(
					info.flatMap((i) -> {
						String fingerPrint = i.getApplicationSshHostKeyFingerprint();
						String host = i.getApplicationSshEndpoint();
						int port = 22; //Default ssh port
						if (host!=null) {
							if (host.contains(":")) {
								String[] pieces = host.split(":");
								host = pieces[0];
								port = Integer.parseInt(pieces[1]);
							}
						}
						if (host!=null) {
							return Mono.just(new SshHost(host, port, fingerPrint));
						}
						// Workaround for bug in Eclipse Neon.1 JDT cannot properly infer type SshHost.
						// Works in Mars. Returning Mono.empty() results in compilation error in Neon.1
						return Mono.<SshHost>empty();
					})
				);
			}

			@Override
			public String getSshCode() throws Exception {
				return ReactorUtils.get(
					log("operations.advanced.sshCode()",
						_operations.advanced().sshCode()
					)
				);
			}
		};
	}

	private Mono<CloudFoundryOperations> operationsFor(OrganizationSummary org) {
		return client_createOperations(org);
	}

	@Override
	public List<CFSpace> getSpaces() throws Exception {
		Object it = ReactorUtils.get(GET_SPACES_TIMEOUT, log("operations.organizations().list()",
				_operations.organizations()
				.list()
			)
			.flatMap((OrganizationSummary org) -> {
				return operationsFor(org).flatMapMany((operations) ->
					log("operations.spaces.list(org="+org.getId()+")",
						operations
						.spaces()
						.list()
						.map((space) -> CFWrappingV2.wrap(org, space))
					)
				);
			})
			.collectList()
		);
		//workaround eclipse jdt bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=501949
		return (List<CFSpace>) it;
	}

	@Override
	public String getHealthCheck(UUID appGuid) throws Exception {
		//XXX CF V2: getHealthcheck (via operations API)
		// See: https://www.pivotaltracker.com/story/show/116462215
		return ReactorUtils.get(
			client_getApplication(appGuid)
			.map((response) -> response.getEntity().getHealthCheckType())
		);
	}

	@Override
	public void setHealthCheck(UUID guid, String hcType) throws Exception {
		//XXX CF V2: setHealthCheck (via operations API)
		// See: https://www.pivotaltracker.com/story/show/116462369
		ReactorUtils.get(
			client_setHealthCheck(guid, hcType)
		);
	}

	@Override
	public List<CFCloudDomain> getDomains() throws Exception {
		return getDomainsOracle()
				.getDomainsList();
	}

	private DomainsOracle getDomainsOracle() {
		return new DomainsOracle();
	}

	/**
	 * DomainsOracle answers questions about domains. It only reads the domain information from
	 * CF once. (However, every newly created instance of DomainsOracle will read the info the
	 * first time it needs it.
	 */
	private class DomainsOracle {
		private Mono<List<CFCloudDomain>> domains = orgId
				.flatMapMany(this::requestDomains)
				.map(CFWrappingV2::wrap)
				.collectList()
				.cache();

		private Mono<Map<String, CFCloudDomain>> domainsByName = domains
				.map(allDomains -> {
					ImmutableMap.Builder<String, CFCloudDomain> builder = ImmutableMap.builder();
					for (CFCloudDomain d : allDomains) {
						builder.put(d.getName(), d);
					}
					return (Map<String, CFCloudDomain>)builder.build();
				})
				.cache();

		public Mono<List<CFCloudDomain>> getDomainsMono() {
			return domains;
		}

		public List<CFCloudDomain> getDomainsList() throws Exception {
			return ReactorUtils.get(domains);
		}

		private Flux<Domain> requestDomains(String orgId) {
			return log("operations.domains.list",
					_operations.domains().list()
			);
		}

		public Mono<Boolean> isTcp(String domainName) {
			return domainsByName.flatMap(dmap -> {
				CFCloudDomain domain = dmap.get(domainName);
				return Mono.just(domain!=null && domain.getType()==CFDomainType.TCP);
			});
		}
	}

	@Override
	public List<CFBuildpack> getBuildpacks() throws Exception {
		//XXX CF V2: getBuilpacks using 'operations' API.
		return ReactorUtils.get(
			PaginationUtils.requestClientV2Resources((page) -> {
				return client_listBuildpacks(page);
			})
			.map(CFWrappingV2::wrap)
			.collectList()
		);
	}

	@Override
	public CFApplicationDetail getApplication(String appName) throws Exception {
		return ReactorUtils.get(
				getApplicationMono(appName)
				//		.log("getApplication("+appName+")")
		);
	}

	private Mono<CFApplicationDetail> getApplicationMono(String appName) {
		return getApplicationDetail(appName)
		.map((appDetail) -> {
			//TODO: we have 'real' appdetails now so we could get most of the 'application extras' info from that.
			return CFWrappingV2.wrap(appDetail, getApplicationExtras(appName));
		})
		.onErrorResume(ReactorUtils.suppressException(IllegalArgumentException.class));
	}

	@Override
	public Version getApiVersion() throws Exception {
		return ReactorUtils.get(info
				.map((i) -> new Version(i.getApiVersion()))
		);
	}

	@Override
	public Version getSupportedApiVersion() {
		return new Version(CloudFoundryClient.SUPPORTED_API_VERSION);
	}

	@Override
	public void deleteApplication(String appName) throws Exception {
		ReactorUtils.get(
			log("operations.applications().delete(name="+appName+")",
				_operations.applications().delete(DeleteApplicationRequest
					.builder()
					.name(appName)
					.build()
				)
			)
		);
	}

	@Override
	public boolean applicationExists(String appName) throws Exception {
		return ReactorUtils.get(
				getApplicationMono(appName)
				.map((app) -> true)
				.switchIfEmpty(Mono.just(false))
		);
	}

	public Mono<Void> ifApplicationExists(String appName, Function<ApplicationDetail, Mono<Void>> then, Mono<Void> els) throws Exception {
		return getApplicationDetail(appName)
		.map((app) -> Optional.of(app))
		.switchIfEmpty(Mono.just(Optional.<ApplicationDetail>empty()))
		.onErrorResume((error) -> Mono.just(Optional.<ApplicationDetail>empty()))
		.flatMap((Optional<ApplicationDetail> app) -> {
			if (app.isPresent()) {
				return then.apply(app.get());
			} else {
				return els;
			}
		});
	}

	@Override
	public void push(CFPushArguments params, CancelationToken cancelationToken) throws Exception {
		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
				v2Push(params)
		);
	}

	private Mono<Void> v2Push(CFPushArguments params) {
		String appName = params.getAppName();

		// Routes are set AFTER push, so for initial push, make sure no route is set
		boolean noRoute = true;

		// Environment variables require app start, so for initial push, do not start app.
		// This will happen afterwards
		boolean noStart = true;
		PushApplicationManifestRequest req = PushApplicationManifestRequest.builder()
				.manifest(ApplicationManifest.builder()
					.name(appName)
					// resource matching occurs under the hood in the push operation
					.path(params.getApplicationDataAsFile().toPath())
					.memory(params.getMemory())
					.disk(params.getDiskQuota())
					.timeout(params.getTimeout())
					.healthCheckType(resolveHealthCheckType(params.getHealthCheckType()).orElse(null))
					.healthCheckHttpEndpoint(params.getHealthCheckHttpEndpoint()==null?"/":params.getHealthCheckHttpEndpoint())
					.buildpack(params.getBuildpack())
					.command(params.getCommand())
					.stack(params.getStack())
					.noRoute(noRoute)
					.instances(params.getInstances())
					.build()
				)
				.noStart(noStart)
				.build();

		return log("client.applications.pushManifest("+req+")",
				_operations.applications().pushManifest(req)
		 )
		.then(mono_debug("Updating routes, bound services, and environment variables..."))
		.then(getApplicationDetail(appName))
		.flatMap((appDetail) -> {
			return Flux.merge(
				setRoutes(appDetail, params.getRoutes(), params.getRandomRoute()),
				bindAndUnbindServices(appName, params.getServices()),
				// This requires app restart
				setEnvVars(appDetail, params.getEnv())
			).then();
		})
		// Start app only after environment variables are set
		.then(params.isNoStart()
				? stopApp(appName)
				: restartApp(appName)
		)
		.then(Mono.empty());
	}

	private Mono<Void> mono_debug(String string) {
		return Mono.fromRunnable(() -> debug(string));
	}

	private Mono<Optional<String>> getStackId(CFPushArguments params) {
		String stackName = params.getStack();
		if (stackName==null) {
			return Mono.just(Optional.empty());
		} else {
			return log("operations.stacks.get("+stackName+")",
				_operations.stacks().get(org.cloudfoundry.operations.stacks.GetStackRequest.builder()
						.name(stackName)
						.build()
				)
				.map((stack) -> Optional.of(stack.getId()))
			);
		}
	}

	private Optional<ApplicationHealthCheck> resolveHealthCheckType(String type) {
		ApplicationHealthCheck appHealthCheck = null;
		if (type != null) {
			try {
				appHealthCheck = ApplicationHealthCheck.from(type);
			} catch (IllegalArgumentException e) {
				Log.log(e);
			}
		}
		return Optional.ofNullable(appHealthCheck);
	}

	private Mono<ApplicationDetail> getApplicationDetail(String appName) {
		return log("operations.applications.get(name="+appName+")",
			_operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
			)
		);
	}

	public Mono<Void> setRoutes(ApplicationDetail appDetails, Collection<String> desiredUrls, boolean randomRoute) {
		debug("setting routes for '"+appDetails.getName()+"': "+desiredUrls+", "+randomRoute);
		DomainsOracle domains = new DomainsOracle();

		//Carefull! It is not safe map/unnmap multiple routes in parallel. Doing so causes some of the
		// operations to fail, presumably because of some 'optimisitic locking' being used in the database
		// that keeps track of routes.
		//To avoid this problem we must execute all that map / unmap calls in sequence!
		return ReactorUtils.sequence(
				unmapUndesiredRoutes(appDetails.getName(), desiredUrls),
				mapDesiredRoutes(appDetails, domains, desiredUrls, randomRoute)
		);
	}

	public Mono<Void> setRoutes(String appName, Collection<String> desiredUrls, boolean randomRoute) {
		return getApplicationDetail(appName)
		.flatMap(appDetails -> setRoutes(appDetails, desiredUrls, randomRoute));
	}

	private Mono<Void> mapDesiredRoutes(ApplicationDetail appDetail, DomainsOracle domains, Collection<String> desiredUrls, boolean randomRoute) {
		Set<String> currentUrls = ImmutableSet.copyOf(appDetail.getUrls());
		String appName = appDetail.getName();

		debug("currentUrls = "+currentUrls);
		return Flux.fromIterable(desiredUrls)
		.flatMap((url) -> {
			if (currentUrls.contains(url)) {
				debug("skipping: "+url);
				return Mono.empty();
			} else {
				debug("mapping: "+url);
				return mapRoute(domains, appName, url, randomRoute);
			}
		}, 1) //!!!IN SEQUENCE!!!
		.then();
	}

	private Mono<Void> mapRoute(DomainsOracle domains, String appName, String desiredUrl, boolean randomRoute) {
		debug("mapRoute: "+appName+" -> "+desiredUrl);
		return toRoute(domains, desiredUrl)
		.flatMap((CFRoute route) -> mapRoute(domains, appName, route, randomRoute))
		.doOnError((e) -> {
			Log.info("mapRoute FAILED!");
			Log.log(e);
		});
	}

	private Mono<Void> mapRoute(DomainsOracle domains, String appName, CFRoute route, boolean randomRoute)  {
		// Let the client validate if any of these combinations are correct.
		// However, only set these values only if they are present as not doing so causes NPE
		Mono<MapRouteRequest.Builder> _builder = Mono.just(MapRouteRequest.builder()
				.applicationName(appName)
		);
		if (StringUtil.hasText(route.getDomain())) {
			_builder = _builder.map(builder -> builder.domain(route.getDomain()));
		}
		if (randomRoute) {
			_builder = _builder.flatMap((MapRouteRequest.Builder builder) ->
				// Can only set random port to true if route is TCP route
				domains.isTcp(route.getDomain()).map(isTcp -> {
					if (isTcp) {
						builder.randomPort(randomRoute);
					} else {
						if (route.getHost()==null) {
							builder.host(appName+"-"+RandomStringUtils.randomAlphabetic(8).toLowerCase());
						}
					}
					return builder;
				})
			);
		}
		_builder = _builder.map(builder -> {
			if (StringUtil.hasText(route.getHost())) {
				builder.host(route.getHost());
			}
			if (StringUtil.hasText(route.getPath())) {
				builder.path(route.getPath());
			}
			if (route.getPort() != CFRoute.NO_PORT) {
				builder.port(route.getPort());
			}
			return builder;
		});
		return _builder.flatMap(builder -> {
			MapRouteRequest mapRouteReq = builder.build();
			return log("operations.routes.map("+mapRouteReq+")",
				_operations.routes().map(mapRouteReq)
			);
		})
		.then();
	}

	private Mono<CFRoute> toRoute(DomainsOracle domains, String desiredUrl) {
		return domains.getDomainsMono().flatMap((ds) -> {
			try {
				CFRoute route = CFRoute.builder().from(desiredUrl, ds).build();
				route.validate();
				return Mono.just(route);
			} catch (Exception e) {
				return Mono.error(e);
			}
		});
	}

//	private Set<String> getUrls(ApplicationDetail app) {
//		return operations.applications().get(GetApplicationRequest.builder()
//				.name(appName)
//				.build()
//		)
//		.map((app) -> ImmutableSet.copyOf(app.getUrls()));
//	}

	private Mono<Void> unmapUndesiredRoutes(String appName, Collection<String> desiredUrls) {
		return getExistingRoutes(appName)
		.flatMap((route) -> {
			debug("unmap? "+route);
			if (desiredUrls.contains(getUrl(route))) {
				debug("unmap? "+route+" SKIP");
				return Mono.empty();
			} else {
				debug("unmap? "+route+" UNMAP");
				return unmapRoute(appName, route);
			}
		}, 1) //!!!IN SEQUENCE!!!
		.then();
	}

	private String getUrl(CFRoute route) {
//		String url = route.getDomain();
//		if (route.getHost()!=null) {
//			url = route.getHost() + "." + url;
//		}
//		String path = route.getPath();
//		if (path!=null) {
//			while (path.startsWith("/")) {
//				path = path.substring(1);
//			}
//			if (StringUtils.hasText(path)) {
//				url = url +"/" +path;
//			}
//		}
		return route.getRoute();
	}

	private Mono<Void> unmapRoute(String appName, CFRoute route) {
//		if (!StringUtil.hasText(path)) {
//			//client doesn't like to get 'empty string' it will complain that route doesn't exist.
//			path = null;
//		}
		org.cloudfoundry.operations.routes.UnmapRouteRequest.Builder unmapBuilder = UnmapRouteRequest.builder()
			.applicationName(appName)
			.domain(route.getDomain())
			.host(route.getHost());

		// Have to check if port and path are set. Cannot just set them without checking
		// otherwise exception throw, even if these values are "empty/null" in the route
		if (route.getPort() != CFRoute.NO_PORT) {
			unmapBuilder.port(route.getPort());
		}
		if (StringUtil.hasText(route.getPath())) {
			unmapBuilder.path(route.getPath());
		}
		UnmapRouteRequest req = unmapBuilder
			.build();
		return log("operations.routes.unmap("+req+")",
			_operations.routes().unmap(req)
		);
	}

	public Flux<CFRoute> getExistingRoutes(String appName) {
		return log("operations.routes.list(level=SPACE)",
			_operations.routes().list(ListRoutesRequest.builder()
				.level(Level.SPACE)
				.build()
			)
		)
		.flatMap((route) -> {
			for (String app : route.getApplications()) {
				if (app.equals(appName)) {
					return Mono.just(CFRoute.builder().from(route).build());
				}
			};
			return Mono.empty();
		});
	}

	public Mono<Void> bindAndUnbindServices(String appName, List<String> _services) {
		debug("bindAndUnbindServices "+_services);
		Set<String> services = ImmutableSet.copyOf(_services);
		return getBoundServicesSet(appName)
		.flatMapMany((boundServices) -> {
			debug("boundServices = "+boundServices);
			Set<String> toUnbind = Sets.difference(boundServices, services);
			Set<String> toBind = Sets.difference(services, boundServices);
			debug("toBind = "+toBind);
			debug("toUnbind = "+toUnbind);
			return Flux.merge(
					bindServices(appName, toBind),
					unbindServices(appName, toUnbind)
			);
		})
		.then();
	}

	public Flux<String> getBoundServices(String appName) {
		return log("operations.services.listInstances()",
			_operations.services().listInstances()
		)
		.filter((service) -> isBoundTo(service, appName))
		.map(ServiceInstanceSummary::getName);
	}

	public Mono<Set<String>> getBoundServicesSet(String appName) {
		return getBoundServices(appName)
		.collectList()
		.map(ImmutableSet::copyOf);
	}

	public Mono<List<String>> getBoundServicesList(String appName) {
		return getBoundServices(appName)
		.collectList()
		.map(ImmutableList::copyOf);
	}

	private boolean isBoundTo(ServiceInstanceSummary service, String appName) {
		return service.getApplications().stream()
				.anyMatch((boundAppName) -> boundAppName.equals(appName));
	}

	private Flux<Void> bindServices(String appName, Set<String> services) {
		return Flux.fromIterable(services)
		.flatMap((service) -> {
			return log("operations.services().bind(appName="+appName+", services="+services+")",
				_operations.services().bind(BindServiceInstanceRequest.builder()
					.applicationName(appName)
					.serviceInstanceName(service)
					.build()
				)
			);
		});
	}

	private Flux<Void> unbindServices(String appName, Set<String> toUnbind) {
		return Flux.fromIterable(toUnbind)
		.flatMap((service) -> {
			return log("operations.services.unbind(appName="+appName+", serviceInstanceName="+service+")",
				_operations.services().unbind(UnbindServiceInstanceRequest.builder()
					.applicationName(appName)
					.serviceInstanceName(service)
					.build()
				)
			);
		});
	}


	protected Mono<Void> startApp(String appName) {
		return log("operations.applications.start(name="+appName+")",
			_operations.applications()
			.start(StartApplicationRequest.builder()
				.name(appName)
				.build()
			)
		);
	}

	private Mono<UUID> getApplicationId(String appName) {
		return log("operations.applications.get(name="+appName+")",
			_operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
			)
		).map((app) -> UUID.fromString(app.getId()));
	}

	public Mono<Void> setEnvVars(ApplicationDetail appDetail, Map<String, String> environment) {
		return setEnvVars(UUID.fromString(appDetail.getId()), environment);
	}

	public Mono<Void> setEnvVars(UUID appId, Map<String, String> environment) {
		return client_setEnv(appId, environment);
	}

	public Mono<Void> setEnvVars(String appName, Map<String, String> environment) {
		return getApplicationId(appName)
		.flatMap((applicationId) -> setEnvVars(applicationId, environment));
	}

//	protected Publisher<? extends Object> setEnvVar(String appName, String var, String value) {
//		System.out.println("Set var starting: "+var +" = "+value);
//		return operations.applications()
//				.setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
//						.name(appName)
//						.variableName(var)
//						.variableValue(value)
//						.build()
//						)
//				.after(() -> {
//					System.out.println("Set var complete: "+var +" = "+value);
//					return Mono.empty();
//				});
//	}

	public Mono<Void> createService(String name, String service, String plan) {
		return log("operations.services.createInstance(instanceName="+name+",serviceName="+service+",planName="+plan+")",
			_operations.services().createInstance(CreateServiceInstanceRequest.builder()
				.serviceInstanceName(name)
				.serviceName(service)
				.planName(plan)
				.build()
			)
		);
	}

	public Mono<Void> createUserProvidedService(String name, Map<String, Object> credentials) {
		return log("operations.services.createUserProvidedInstance(name="+name+")",
			_operations.services().createUserProvidedInstance(CreateUserProvidedServiceInstanceRequest.builder()
				.name(name)
				.credentials(credentials)
				.build()
			)
		);
	}

//	@Override
//	public void deleteService(String serviceName) {
//		deleteServiceMono(serviceName).get();
//	}

	@Override
	public Mono<Void> deleteServiceAsync(String serviceName) {
		return getService(serviceName)
		.flatMap(this::deleteServiceInstance);
	}

	protected Mono<Void> deleteServiceInstance(ServiceInstance s) {
		switch (s.getType()) {
		case MANAGED:
			return client_deleteServiceInstance(s);
		case USER_PROVIDED:
			return client_deleteUserProvidedService(s);
		default:
			return Mono.error(new IllegalStateException("Unknown service type: "+s.getType()));
		}
	}

	protected Mono<ServiceInstance> getService(String serviceName) {
		return log("operations.services.getInstance(name="+serviceName+")",
			_operations.services().getInstance(GetServiceInstanceRequest.builder()
				.name(serviceName)
				.build()
			)
		);
	}

	public Mono<Map<String,String>> getEnv(String appName) {
		return log("operations.applications.getEnvironments(appName="+appName+")",
			_operations.applications().getEnvironments(GetApplicationEnvironmentsRequest.builder()
				.name(appName)
				.build()
			)
		)
		.map((envs) -> envs.getUserProvided())
		.map(this::dropObjectsFromMap);
	}

	@Override
	public Map<String, String> getApplicationEnvironment(String appName) throws Exception {
		return ReactorUtils.get(getEnv(appName));
	}

	private Map<String, String> dropObjectsFromMap(Map<String, Object> map) {
		Builder<String, String> builder = ImmutableMap.builder();
		for (Entry<String, Object> entry : map.entrySet()) {
			try {
				builder.put(entry.getKey(), (String) entry.getValue());
			} catch (ClassCastException e) {
				Log.log(e);
			}
		}
		return builder.build();
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

	private Mono<GetInfoResponse> client_getInfo() {
		return log("client.info().get()",
				_client.info().get(GetInfoRequest.builder().build())
		);
	}

	private Mono<String> operations_getOrgId() {
		return log("operations.organizations.get(name="+params.getOrgName()+")",
				_operations.organizations().get(OrganizationInfoRequest.builder()
				.name(params.getOrgName())
				.build()
			)
			.map(OrganizationDetail::getId)
		);
	}

	private Mono<ImmutableList<CFApplication>> operations_listApps() {
		return log("operations.applications.list()",
			_operations.applications()
			.list()
			.map((appSummary) ->
				CFWrappingV2.wrap(appSummary, getApplicationExtras(appSummary.getName()))
			)
			.collectList()
			.map(ImmutableList::copyOf)
		);
	}

	private Mono<GetApplicationResponse> client_getApplication(UUID appId) {
		return log("client.applicationsV2.get(id="+appId+")",
			_client.applicationsV2()
			.get(org.cloudfoundry.client.v2.applications.GetApplicationRequest.builder()
					.applicationId(appId.toString())
					.build()
			)
		);
	}

	private Mono<GetStackResponse> client_getStack(String stackId) {
		return log("client.stacks.get(id="+stackId+")",
			_client.stacks().get(GetStackRequest.builder()
				.stackId(stackId)
				.build()
			)
		);
	}

	private Mono<UpdateApplicationResponse> client_setHealthCheck(UUID guid, String hcType) {
		return log("client.applicationsV2.update(id="+guid+", hcType="+hcType+")",
		_client.applicationsV2()
			.update(UpdateApplicationRequest.builder()
				.applicationId(guid.toString())
				.healthCheckType(hcType)
				.build()
			)
		);
	}

	private Mono<ListBuildpacksResponse> client_listBuildpacks(Integer page) {
		return log("client.buildpacks.list(page="+page+")",
			_client.buildpacks()
			.list(ListBuildpacksRequest.builder()
				.page(page)
				.build()
			)
		);
	}

	private Mono<Void> client_setEnv(UUID appId, Map<String, String> environment) {
		return log("client.applicationsV2.update(id="+appId+", env=...)",
			_client.applicationsV2()
			.update(UpdateApplicationRequest.builder()
				.applicationId(appId.toString())
				.environmentJsons(environment)
				.build())
			.then()
		);
	}

	private Mono<Void> client_deleteServiceInstance(ServiceInstance s) {
		return log("client.serviceInstances.delete(id="+s.getId()+")",
			_client.serviceInstances().delete(DeleteServiceInstanceRequest.builder()
					.serviceInstanceId(s.getId())
					.build()
			)
			.then()
		);
	}

	private Mono<Void> client_deleteUserProvidedService(ServiceInstance s) {
		return log("client.userProvidedServiceInstances.delete(id="+s.getId()+")",
			_client.userProvidedServiceInstances().delete(DeleteUserProvidedServiceInstanceRequest.builder()
				.userProvidedServiceInstanceId(s.getId())
				.build()
			)
		);
	}

	@Override
	public Mono<String> getUserName() {
		return log("uaa.getUsername",
				/*
				 * UAA times out at start until the client is cached. Needs ~2 mins to fetch the user.
				 * Instead get userID from client info and then fetch the user complete data from id to get the user name. This works and bypasses UAA
				 */
//				_uaa.getUsername()
				info
					.flatMap(info -> _client
							.users()
							.get(GetUserRequest.builder().userId(info.getUser()).build())
							.map(response -> response.getEntity().getUsername()))
		).timeout(GET_USERNAME_TIMEOUT);
	}

	@Override
	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public Flux<String>	getRefreshTokens() {
		return refreshTokensFlux;
	}

}
