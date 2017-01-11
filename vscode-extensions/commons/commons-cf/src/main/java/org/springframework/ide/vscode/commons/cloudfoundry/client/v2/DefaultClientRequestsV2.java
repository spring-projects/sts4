/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.CreateApplicationRequest;
import org.cloudfoundry.client.v2.applications.GetApplicationResponse;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v2.applications.UpdateApplicationResponse;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksResponse;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.client.v2.domains.ListDomainsResponse;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.cloudfoundry.client.v2.info.GetInfoResponse;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.stacks.GetStackRequest;
import org.cloudfoundry.client.v2.stacks.GetStackResponse;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.DeleteUserProvidedServiceInstanceRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;
import org.cloudfoundry.reactor.tokenprovider.AbstractUaaTokenProvider;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.util.PaginationUtils;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplication;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFSpace;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.SshClientSupport;
import org.springframework.ide.vscode.commons.cloudfoundry.client.SshHost;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CancelationTokens.CancelationToken;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientCache.CFClientProvider;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private static final Duration APP_START_TIMEOUT = Duration.ofMillis(60*10);
	private static final Duration GET_SERVICES_TIMEOUT = Duration.ofSeconds(60);
	private static final Duration GET_SPACES_TIMEOUT = Duration.ofSeconds(20);
	private static final Duration GET_USERNAME_TIMEOUT = Duration.ofSeconds(5);
    private static final Logger logger = Logger.getLogger(DefaultClientRequestsV2.class.getName());
	private static final boolean DEBUG = false;



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


// TODO: it would be good not to create another 'threadpool' and use something like the below code
//  instead so that eclipse job scheduler is used for reactor 'tasks'. However... the code below
//  may not be 100% correct.
//	private static final Callable<? extends Consumer<Runnable>> SCHEDULER_GROUP = () -> {
//		return (Runnable task) -> {
//			Job job = new Job("CF Client background task") {
//				@Override
//				protected IStatus run(IProgressMonitor monitor) {
//					if (task!=null) {
//						task.run();
//					}
//					return Status.OK_STATUS;
//				}
//			};
//			job.setRule(JobUtil.lightRule("reactor-job-rule"));
//			job.setSystem(true);
//			job.schedule();
//		};
//	};


	private CFClientParams params;
	private CloudFoundryClient _client ;
	private UaaClient _uaa;
	private CloudFoundryOperations _operations;

	private Mono<String> orgId;
	private Mono<GetInfoResponse> info;
	private Mono<String> spaceId;
	private AbstractUaaTokenProvider _tokenProvider;

	public DefaultClientRequestsV2(CloudFoundryClientCache clients, CFClientParams params) {
		this.params = params;
		CFClientProvider provider = clients.getOrCreate(params.getUsername(), params.getCredentials(), params.getHost(), params.skipSslValidation());
		this._client = provider.client;
		this._uaa = provider.uaaClient;
		this._tokenProvider = (AbstractUaaTokenProvider) provider.tokenProvider;
		this._operations = DefaultCloudFoundryOperations.builder()
				.cloudFoundryClient(_client)
				.dopplerClient(provider.doppler)
				.uaaClient(provider.uaaClient)
				.organization(params.getOrgName())
				.space(params.getSpaceName())
				.build();
		this.orgId = getOrgId();
		this.spaceId = getSpaceId();
		this.info = client_getInfo().cache();
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

	private Mono<String> getSpaceId() {
		String spaceName = params.getSpaceName();
		if (spaceName==null) {
			return Mono.error(new IOException("No space targetted"));
		} else {
			return _operations.spaces().get(GetSpaceRequest.builder()
				.name(params.getSpaceName())
				.build()
			)
			.map(SpaceDetail::getId)
			.cache();
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
			.then((appId) ->
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
				entity.then((e) -> Mono.justOrEmpty(e.getBuildpack()))
		);

		Mono<String> stack = prefetch("stack",
			entity.then((e) -> Mono.justOrEmpty(e.getStackId()))
			.then((stackId) -> {
				return client_getStack(stackId);
			}).map((response) -> {
				return response.getEntity().getName();
			})
		);
		Mono<Integer> timeout = prefetch("timeout",
				entity
				.then((v) -> Mono.justOrEmpty(v.getHealthCheckTimeout()))
		);

		Mono<String> command = prefetch("command",
				entity.then((e) -> Mono.justOrEmpty(e.getCommand()))
		);

		Mono<String> healthCheckType = prefetch("healthCheckType",
				entity.then((e) -> Mono.justOrEmpty(e.getHealthCheckType()))
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
		};
	}

	private <T> Mono<T> prefetch(String id, Mono<T> toFetch) {
		return toFetch
//		.log(id + " before error handler")
		.otherwise((error) -> {
			logger.log(Level.SEVERE,"Failed prefetch '"+id+"'", error);
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
				.map(CFWrappingV2::wrap)
				.collectList()
				.map(ImmutableList::copyOf)
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
			.otherwise((error) -> {
				logger.log(Level.SEVERE,"getting application details for '"+appSummary.getName()+"' failed", error);

				return Mono.empty();
			})
			.map((ApplicationDetail appDetails) -> CFWrappingV2.wrap((CFApplicationSummaryData)appSummary, appDetails));
		});
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
	public void logout() {
		_operations = null;
		_client = null;
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
					info.then((i) -> {
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
				return operationsFor(org).flatMap((operations) ->
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
		//XXX CF V2: list domains using 'operations' api.
		return ReactorUtils.get(Duration.ofMinutes(2),
			orgId.flatMap(this::requestDomains)
			.map(CFWrappingV2::wrap)
			.collectList()
		);
	}

	private Flux<DomainResource> requestDomains(String orgId) {
		return PaginationUtils.requestClientV2Resources((page) ->
			client_listDomains(page)
		);
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
		.otherwise(ReactorUtils.suppressException(IllegalArgumentException.class));
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
				.otherwiseIfEmpty(Mono.just(false))
		);
	}

	public Mono<Void> ifApplicationExists(String appName, Function<ApplicationDetail, Mono<Void>> then, Mono<Void> els) throws Exception {
		return getApplicationDetail(appName)
		.map((app) -> Optional.of(app))
		.otherwiseIfEmpty(Mono.just(Optional.<ApplicationDetail>empty()))
		.otherwise((error) -> Mono.just(Optional.<ApplicationDetail>empty()))
		.then((Optional<ApplicationDetail> app) -> {
			if (app.isPresent()) {
				return then.apply(app.get());
			} else {
				return els;
			}
		});
	}

	@Override
	public void push(CFPushArguments params, CancelationToken cancelationToken) throws Exception {
		String appName = params.getAppName();
		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
			ifApplicationExists(appName,
				((app) -> pushExisting(app, params)),
				firstPush(params)
			)
		);
	}

	private Mono<Void> pushExisting(ApplicationDetail app, CFPushArguments params) {
		// TODO: push is using v1 below which was not ported to vscode CF support.
//		String appName = params.getAppName();
//		UUID appId = UUID.fromString(app.getId());
//		return updateApp(appId, params)
//		.then(getApplicationDetail(appName))
//		.then((appDetail) -> {
//			return Flux.merge(
//				setRoutes(appDetail, params.getRoutes()),
//				bindAndUnbindServices(appName, params.getServices())
//			).then();
//		})
//		.then(mono_debug("Uploading[1]..."))
//		.then(Mono.fromCallable(() -> {
//			debug("Uploading[2]...");
//			v1().uploadApplication(appName, params.getApplicationData());
//			debug("Uploading[2] DONE");
//			return "who cares";
//		}))
//		.then(mono_debug("Uploading[1] DONE"))
//		.then(params.isNoStart()
//			? stopApp(appName)
//			: restartApp(appName)
//		);
		throw new Error("Application push not currently supported in CF vscode");
	}

//	private DefaultClientRequestsV1 v1() throws Exception {
//		if (_v1==null) {
//			CFClientParams v1params = new CFClientParams(
//					params.getApiUrl(),
//					params.getUsername(),
//					CFCredentials.fromRefreshToken(getRefreshToken()),
//					params.isSelfsigned(),
//					params.getOrgName(),
//					params.getSpaceName(),
//					params.skipSslValidation()
//			);
//			_v1 = new DefaultClientRequestsV1(v1params);
//		}
//		return _v1;
//	}

	private Mono<Void> mono_debug(String string) {
		return Mono.fromRunnable(() -> debug(string));
	}

	private Mono<Void> firstPush(CFPushArguments params) {
		String appName = params.getAppName();
		return createApp(params)
		.then(getApplicationDetail(appName))
		.then((appDetail) ->
			Flux.merge(
				setRoutes(appDetail, params.getRoutes()),
				bindAndUnbindServices(appName, params.getServices())
			).then()
		)
		.then(Mono.fromCallable(() -> {
//			v1().uploadApplication(appName, params.getApplicationData());
			return "who cares";
		}))
		.then(params.isNoStart()
			? Mono.empty()
			: startApp(appName)
		)
		.then();
	}

	private Mono<UUID> createApp(CFPushArguments params) {
		return spaceId.and(getStackId(params))
		.then(function((spaceId, stackId) -> {
			CreateApplicationRequest req = CreateApplicationRequest.builder()
					.spaceId(spaceId)
					.stackId(stackId.orElse(null))
					.name(params.getAppName())
					.memory(params.getMemory())
					.diskQuota(params.getDiskQuota())
					.healthCheckType(params.getHealthCheckType())
					.healthCheckTimeout(params.getTimeout())
					.buildpack(params.getBuildpack())
					.command(params.getCommand())
					.environmentJsons(params.getEnv())
					.instances(params.getInstances())
					.build();
			return log("client.applications.create("+req+")",
				_client.applicationsV2().create(req)
			);
		}))
		.map(response -> UUID.fromString(response.getMetadata().getId()));
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


	private Mono<UUID> updateApp(UUID appId, CFPushArguments params) {
		return getStackId(params)
		.then((stackId) -> {
			UpdateApplicationRequest req = UpdateApplicationRequest.builder()
				.applicationId(appId.toString())
				.name(params.getAppName())
				.memory(params.getMemory())
				.diskQuota(params.getDiskQuota())
				.healthCheckType(params.getHealthCheckType())
				.healthCheckTimeout(params.getTimeout())
				.buildpack(params.getBuildpack())
				.command(params.getCommand())
				.stackId(stackId.orElse(null))
				.environmentJsons(params.getEnv())
				.instances(params.getInstances())
				.build();
			return log("client.applications.update("+req+")",
				_client.applicationsV2().update(req)
			);
		})
		.then(Mono.just(appId));
	}

//	public void pushV2(CFPushArguments params, CancelationToken cancelationToken) throws Exception {
//		debug("Pushing app starting: "+params.getAppName());
//		//XXX CF V2: push should use 'manifest' in a future version of V2
//		PushApplicationRequest pushRequest = toPushRequest(params)
//				.noStart(true)
//				.noRoute(true)
//				.build();
//		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
//			log("operations.applications().push("+pushRequest+")",
//				_operations.applications()
//				.push(pushRequest)
//			)
//			.then(getApplicationDetail(params.getAppName()))
//			.then(appDetail -> {
//				return Flux.merge(
//					setRoutes(appDetail, params.getRoutes()),
//					setEnvVars(appDetail, params.getEnv()),
//					bindAndUnbindServices(params.getAppName(), params.getServices())
//				)
//				.then();
//			})
//			.then(() -> {
//				if (!params.isNoStart()) {
//					return startApp(params.getAppName());
//				} else {
//					return Mono.empty();
//				}
//			})
//		);
//		debug("Pushing app succeeded: "+params.getAppName());
//	}

	private Mono<ApplicationDetail> getApplicationDetail(String appName) {
		return log("operations.applications.get(name="+appName+")",
			_operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
			)
		);
	}

	public Mono<Void> setRoutes(ApplicationDetail appDetails, Collection<String> desiredUrls) {
		debug("setting routes for '"+appDetails.getName()+"': "+desiredUrls);

		//Carefull! It is not safe map/unnmap multiple routes in parallel. Doing so causes some of the
		// operations to fail, presumably because of some 'optimisitic locking' being used in the database
		// that keeps track of routes.
		//To avoid this problem we must execute all that map / unmap calls in sequence!
		return ReactorUtils.sequence(
				unmapUndesiredRoutes(appDetails.getName(), desiredUrls),
				mapDesiredRoutes(appDetails, desiredUrls)
		);
	}

	public Mono<Void> setRoutes(String appName, Collection<String> desiredUrls) {
		return getApplicationDetail(appName)
		.then(appDetails -> setRoutes(appDetails, desiredUrls));
	}

	private Mono<Void> mapDesiredRoutes(ApplicationDetail appDetail, Collection<String> desiredUrls) {
		Set<String> currentUrls = ImmutableSet.copyOf(appDetail.getUrls());
		Mono<Set<String>> domains = getDomainNames().cache();
		String appName = appDetail.getName();

		debug("currentUrls = "+currentUrls);
		return Flux.fromIterable(desiredUrls)
		.flatMap((url) -> {
			if (currentUrls.contains(url)) {
				debug("skipping: "+url);
				return Mono.empty();
			} else {
				debug("mapping: "+url);
				return mapRoute(domains, appName, url);
			}
		}, 1) //!!!IN SEQUENCE!!!
		.then();
	}

	private Mono<Void> mapRoute(Mono<Set<String>> domains, String appName, String desiredUrl) {
		debug("mapRoute: "+appName+" -> "+desiredUrl);
		return toRoute(domains, desiredUrl)
		.then((CFRoute route) -> mapRoute(appName, route))
		.doOnError((e) -> {
			logger.info("mapRoute FAILED!");
		});
	}

	private Mono<Void> mapRoute(String appName, CFRoute route) {
		MapRouteRequest mapRouteReq = MapRouteRequest.builder()
				.applicationName(appName)
				.domain(route.getDomain())
				.host(route.getHost())
				.path(route.getPath())
				.build();
		return log("operations.routes.map("+mapRouteReq+")",
			_operations.routes().map(mapRouteReq)
		);
	}

	private Mono<CFRoute> toRoute(Mono<Set<String>> domains, String desiredUrl) {
		return domains.then((ds) -> {
			for (String d : ds) {
				//TODO: we assume that there's no 'path' component for now, which simpiflies things. What if there is a path component?
				if (desiredUrl.endsWith(d)) {
					String host = desiredUrl.substring(0, desiredUrl.length()-d.length());
					while (host.endsWith(".")) {
						host = host.substring(0, host.length()-1);
					}
					CFRoute.Builder route = CFRoute.builder();
					route.domain(d);
					if (StringUtils.hasText(host)) {
						route.host(host);
					}
					return Mono.just(route.build());
				}
			}
			return Mono.error(new IOException("Couldn't find a domain matching "+desiredUrl));
		});
	}

	private Mono<Set<String>> getDomainNames() {
		return orgId.flatMap(this::requestDomains)
		.map((r) -> r.getEntity().getName())
		.collectList()
		.map(ImmutableSet::copyOf);
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
		String url = route.getDomain();
		if (route.getHost()!=null) {
			url = route.getHost() + "." + url;
		}
		String path = route.getPath();
		if (path!=null) {
			while (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (StringUtils.hasText(path)) {
				url = url +"/" +path;
			}
		}
		return url;
	}

	private Mono<Void> unmapRoute(String appName, CFRoute route) {
		String path = route.getPath();
//		if (!StringUtil.hasText(path)) {
//			//client doesn't like to get 'empty string' it will complain that route doesn't exist.
//			path = null;
//		}
		UnmapRouteRequest req = UnmapRouteRequest.builder()
			.applicationName(appName)
			.domain(route.getDomain())
			.host(route.getHost())
			.path(path)
			.build();
		return log("operations.routes.unmap("+req+")",
			_operations.routes().unmap(req)
		);
	}

	public Flux<CFRoute> getExistingRoutes(String appName) {
		return log("operations.routes.list(level=SPACE)",
			_operations.routes().list(ListRoutesRequest.builder()
				.level(org.cloudfoundry.operations.routes.Level.SPACE)
				.build()
			)
		)
		.flatMap((route) -> {
			for (String app : route.getApplications()) {
				if (app.equals(appName)) {
					return Mono.just(new CFRoute(route));
				}
			};
			return Mono.empty();
		});
	}

//	private static PushApplicationRequest.Builder toPushRequest(CFPushArguments params) {
//		return PushApplicationRequest.builder()
//		.name(params.getAppName())
//		.memory(params.getMemory())
//		.diskQuota(params.getDiskQuota())
//		.timeout(params.getTimeout())
//		.buildpack(params.getBuildpack())
//		.command(params.getCommand())
//		.stack(params.getStack())
//		.instances(params.getInstances())
//		.application(params.getApplicationData());
//	}

	public Mono<Void> bindAndUnbindServices(String appName, List<String> _services) {
		debug("bindAndUnbindServices "+_services);
		Set<String> services = ImmutableSet.copyOf(_services);
		return getBoundServicesSet(appName)
		.flatMap((boundServices) -> {
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
		.map(ServiceInstance::getName);
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

	private boolean isBoundTo(ServiceInstance service, String appName) {
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
		.then((applicationId) -> setEnvVars(applicationId, environment));
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
		.then(this::deleteServiceInstance);
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
				logger.log(Level.SEVERE,e.getMessage(), e);

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

	private void debug(String string) {
		// TODO Auto-generated method stub
		
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

	private Mono<ListDomainsResponse> client_listDomains(Integer page) {
		return log("client.domains.list(page="+page+")",
			_client.domains().list(ListDomainsRequest.builder()
				.page(page)
				//						.owningOrganizationId(orgId)
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
	public String getRefreshToken() {
		return _tokenProvider.getRefreshToken();
	}

	@Override
	public Mono<String> getUserName() {
		return log("uaa.getUsername",
				_uaa.getUsername()
		).timeout(GET_USERNAME_TIMEOUT);
	}

}
