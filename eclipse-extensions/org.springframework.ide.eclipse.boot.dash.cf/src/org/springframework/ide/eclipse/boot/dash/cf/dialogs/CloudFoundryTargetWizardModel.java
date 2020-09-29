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
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.operation.IRunnableContext;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials.LoginMethod;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFExceptions;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.WizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.CannotAccessPropertyException;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableSet;

/**
 * Cloud Foundry Target properties that uses {@link LiveExpression} and
 * {@link Validator}.
 */
public class CloudFoundryTargetWizardModel {

	private RunTargetType runTargetType;
	private BootDashModelContext context;

	private final LiveVariable<String> url = new LiveVariable<>();
	private final LiveVariable<CFSpace> space = new LiveVariable<>();
	private final LiveVariable<Boolean> selfsigned = new LiveVariable<>(false);
	private final LiveVariable<Boolean> skipSslValidation = new LiveVariable<>(false);
	private final LiveVariable<LoginMethod> method = new LiveVariable<>(LoginMethod.PASSWORD);
	private final LiveVariable<String> userName = new LiveVariable<>();
	private final LiveVariable<String> password = new LiveVariable<>();
	private final LiveVariable<StoreCredentialsMode> storeCredentials = new LiveVariable<>(StoreCredentialsMode.STORE_NOTHING);

	private final LiveVariable<ValidationResult> spaceResolutionStatus = new LiveVariable<>(ValidationResult.OK); // has an error if resolution failed.
	private final LiveVariable<OrgsAndSpaces> resolvedSpaces = new LiveVariable<>();

	private String refreshToken = null;

	private final Validator credentialsValidator = new Validator() {
		{
			dependsOn(url);
			dependsOn(userName);
			dependsOn(method);
			dependsOn(password);
		}

		@Override
		protected ValidationResult compute() {
			if (isEmpty(userName.getValue()) && method.getValue()==LoginMethod.PASSWORD) {
				return ValidationResult.info("Enter a username");
			} else if (isEmpty(url.getValue())) {
				try {
					new URL(url.getValue());
					return ValidationResult.info("Enter a target URL");
				} catch (MalformedURLException e) {
					return ValidationResult.error(e.getMessage());
				}
			} else if (method.getValue()==LoginMethod.PASSWORD) {
				if (isEmpty(password.getValue())) {
					return ValidationResult.info("Enter a password");
				}
			} else if (method.getValue()==LoginMethod.TEMPORARY_CODE) {
				if (isEmpty(password.getValue())) {
					return ValidationResult.info("Enter a Temporary Access Code");
				}
			}
			return ValidationResult.OK;
		}

		protected boolean isEmpty(String value) {
			return value == null || value.trim().length() == 0;
		}
	};

	private final Validator spaceValidator = new Validator() {
		{
			dependsOn(space);
		}
		@Override
		protected ValidationResult compute() {
			if (getSpaceName() == null || getOrganizationName() == null) {
				return ValidationResult.info("Select a Cloud space");
			}

			if (space.getValue() != null) {
				RunTarget existing = CloudFoundryTargetWizardModel.this.getExistingRunTarget(space.getValue());
				if (existing != null) {
					return ValidationResult.error("A run target for that space already exists: '" + existing.getName()
							+ "'. Please select another space.");
				}
			}
			return ValidationResult.OK;
		}
	};
	private Validator resolvedSpacesValidator = new Validator() {
		{
			dependsOn(spaceResolutionStatus);
			dependsOn(resolvedSpaces);
		}
		@Override
		protected ValidationResult compute() {
			ValidationResult resolveStatus = spaceResolutionStatus.getValue();
			if (!resolveStatus.isOk()) {
				return resolveStatus;
			}
			if (resolvedSpaces.getValue() == null || resolvedSpaces.getValue().getAllSpaces() == null) {
				return ValidationResult.info("Select a space to validate the credentials.");
			}
			if (resolvedSpaces.getValue().getAllSpaces().isEmpty()) {
				return ValidationResult.error(
						"No spaces available to select. Please check that the credentials and target URL are correct, and spaces are defined in the target.");
			}
			return ValidationResult.OK;
		}
	};
	private Validator storeCredentialsValidator = PasswordDialogModel.makeStoreCredentialsValidator(method, storeCredentials);
	private CompositeValidator allPropertiesValidator = new CompositeValidator();

	private CloudFoundryClientFactory clientFactory;
	private ImmutableSet<RunTarget> existingTargets;
	private WizardModelUserInteractions interactions;


	public CloudFoundryTargetWizardModel(RunTargetType runTargetType, CloudFoundryClientFactory clientFactory,
			ImmutableSet<RunTarget> existingTargets, BootDashModelContext context) {
		this(runTargetType, clientFactory, existingTargets, context, null);
	}

	public CloudFoundryTargetWizardModel(RunTargetType runTargetType, CloudFoundryClientFactory clientFactory,
			ImmutableSet<RunTarget> existingTargets, BootDashModelContext context, WizardModelUserInteractions interactions) {
		this.runTargetType = runTargetType;
		this.context = context;
		Assert.isNotNull(clientFactory, "clientFactory should not be null");
		this.interactions = interactions;
		this.existingTargets = existingTargets == null ? ImmutableSet.<RunTarget>of() : existingTargets;
		this.clientFactory = clientFactory;

		// Aggregate of the credentials and space validators.
		allPropertiesValidator.addChild(credentialsValidator);
		allPropertiesValidator.addChild(storeCredentialsValidator);
		allPropertiesValidator.addChild(resolvedSpacesValidator);
		allPropertiesValidator.addChild(spaceValidator);

		url.setValue(getDefaultTargetUrl());
	}

	public void setUrl(String url) {
		this.url.setValue(url);
	}

	public void setSelfsigned(boolean selfsigned) {
		this.selfsigned.setValue(selfsigned);
	}

	public void skipSslValidation(boolean skipSsl) {
		this.skipSslValidation.setValue(skipSsl);
	}

	public void setUsername(String userName) {
		this.userName.setValue(userName);
	}

	public void setPassword(String password) throws CannotAccessPropertyException {
		this.password.setValue(password);
	}

	public void setSpace(CFSpace space) {
		this.space.setValue(space);
	}

	public String getPassword() throws CannotAccessPropertyException {
		return password.getValue();
	}

	public String getUrl() {
		return url.getValue();
	}

	public String getUsername() {
		return userName.getValue();
	}

	public void setStoreCredentials(StoreCredentialsMode store) {
		storeCredentials.setValue(store);
	}

	public StoreCredentialsMode getStoreCredentials() {
		return storeCredentials.getValue();
	}

	protected String getDefaultTargetUrl() {
		return "https://api.run.pivotal.io";
	}

	public OrgsAndSpaces resolveSpaces(IRunnableContext context) {
		try {
			boolean toFetchSpaces = true;
			OrgsAndSpaces spaces = getCloudSpaces(createTargetProperties(toFetchSpaces), context);
			resolvedSpaces.setValue(spaces);
			spaceResolutionStatus.setValue(ValidationResult.OK);
			return resolvedSpaces.getValue();
		} catch (Exception e) {
			if (CFExceptions.isAuthFailure(e) || CFExceptions.isSSLCertificateFailure(e)) {
				//don't log, its expected if user just typed bad password,
				//or didn't check ssl box when they should have.
			} else {
				Log.log(e);
			}
			resolvedSpaces.setValue(null);
			spaceResolutionStatus.setValue(ValidationResult.error(ExceptionUtil.getMessage(e)));
			return null;
		}
	}

	private OrgsAndSpaces getCloudSpaces(final CloudFoundryTargetProperties targetProperties, IRunnableContext context)
			throws Exception {

		OrgsAndSpaces spaces = null;

		Operation<List<CFSpace>> op = new Operation<List<CFSpace>>(
				"Connecting to the Cloud Foundry target. Please wait while the list of spaces is resolved...") {
			@Override
			protected List<CFSpace> runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
				ClientRequests client = clientFactory.getClient(targetProperties);
				try {
					List<CFSpace> spaces = client.getSpaces();
					String t = client.getRefreshToken();
					if (t!=null) {
						refreshToken = t;
					}
					String effectiveUser = getUserName(client);
					if (effectiveUser!=null) {
						userName.setValue(effectiveUser);
					}
					return spaces;
				} finally {
					client.close();
				}
			}

			private String getUserName(ClientRequests client) {
				try {
					return client.getUserName().block();
				} catch (Exception e) {
					Log.log(e);
				}
				return null;
			}
		};

		List<CFSpace> actualSpaces = op.run(context, true);
		if (actualSpaces != null && !actualSpaces.isEmpty()) {
			spaces = new OrgsAndSpaces(actualSpaces);
		}

		return spaces;
	}

	/**
	 * Create target properties based on current input values in the wizard.
	 * <p>
	 * Note that there are two slightly different ways to produce these properties.
	 * <p>
	 * a) to create a intermediate client just to fetch orgs and spaces.
	 * <p>
	 * b) the final properties used to create the client after space is selected and the user
	 * clicks 'finish' button.
	 */
	private CloudFoundryTargetProperties createTargetProperties(boolean toFetchSpaces) throws CannotAccessPropertyException {
		CloudFoundryTargetProperties targetProps = new CloudFoundryTargetProperties(null, runTargetType, context.injections);
		if (!toFetchSpaces) {
			//Take care: when fetching spaces the space may not be known yet, so neither is the id
			String id = CloudFoundryTargetProperties.getId(
					this.getUsername(),
					this.getUrl(),
					this.getOrganizationName(),
					this.getSpaceName()
			);
			targetProps.put(TargetProperties.RUN_TARGET_ID, id);
		}

		targetProps.setUrl(url.getValue());
		targetProps.setSelfSigned(selfsigned.getValue());
		targetProps.setSkipSslValidation(skipSslValidation.getValue());

		targetProps.setUserName(userName.getValue());
		if (toFetchSpaces) {
			targetProps.setStoreCredentials(StoreCredentialsMode.STORE_NOTHING);
			targetProps.setCredentials(CFCredentials.fromLogin(method.getValue(), password.getValue()));
		} else {
			//use credentials of a style that is consistent with the 'store mode'.
			if (method.getValue()==LoginMethod.TEMPORARY_CODE && storeCredentials.getValue()==StoreCredentialsMode.STORE_PASSWORD) {
				//The temporary token shouldn't be stored since its meaningless. Silently downgrade storemode:
				storeCredentials.setValue(StoreCredentialsMode.STORE_NOTHING);
			}
			StoreCredentialsMode mode = storeCredentials.getValue();
			targetProps.setStoreCredentials(storeCredentials.getValue());
			switch (mode) {
			case STORE_NOTHING:
			case STORE_TOKEN:
				Assert.isTrue(refreshToken!=null);
				targetProps.setCredentials(CFCredentials.fromRefreshToken(refreshToken));
				break;
			case STORE_PASSWORD:
				targetProps.setCredentials(CFCredentials.fromPassword(password.getValue()));
				break;
			default:
				throw new IllegalStateException("BUG: Missing switch case?");
			}
		}
		targetProps.setSpace(space.getValue());
		return targetProps;
	}

	public OrgsAndSpaces getSpaces() {
		return resolvedSpaces.getValue();
	}

	protected RunTarget getExistingRunTarget(CFSpace space) {
		if (space != null) {
			String targetId = CloudFoundryTargetProperties.getId(getUsername(), getUrl(),
					space.getOrganization().getName(), space.getName());
			for (RunTarget target : existingTargets) {
				if (targetId.equals(target.getId())) {
					return target;
				}
			}
		}
		return null;
	}

	public CloudFoundryRunTarget finish() throws Exception {
		CloudFoundryTargetProperties targetProps = null;
		try {
			targetProps = createTargetProperties(/*toFetchSpaces*/false);
		} catch (Exception e) {
			final StorageException storageException = getStorageException(e);
			// Allow run target to be created on storage exceptions as the run target can still be created and connected
			if (storageException != null) {
				Log.log(storageException);
				if (interactions != null) {
					String message = "Failed to store credentials in secure storage. Please check your secure storage preferences. Error: "
							+ storageException.getMessage();
					interactions.informationPopup("Secure Storage Error", message);
				}
				storeCredentials.setValue(StoreCredentialsMode.STORE_NOTHING);
				targetProps = createTargetProperties(/*toFetchSpaces*/false);
			} else {
				throw e;
			}
		}
		return (CloudFoundryRunTarget) runTargetType.createRunTarget(targetProps);
	}

	public String getSpaceName() {
		CFSpace space = this.space.getValue();
		if (space!=null) {
			return space.getName();
		}
		return null;
	}

	public String getOrganizationName() {
		CFSpace space = this.space.getValue();
		if (space!=null) {
			return space.getOrganization().getName();
		}
		return null;
	}

	protected StorageException getStorageException(Exception e) {
		if (e instanceof StorageException) {
			return (StorageException) e;
		}
		if (e.getCause() instanceof StorageException) {
			return (StorageException) e.getCause();
		}
		return null;
	}

	/**
	 * @return A 'complete' validator that reflects the validation state of all the inputs in this 'ui'.
	 */
	public LiveExpression<ValidationResult> getValidator() {
		return allPropertiesValidator;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public LiveVariable<LoginMethod> getMethodVar() {
		return method;
	}

	public LiveVariable<String> getUserNameVar() {
		return userName;
	}

	public LiveVariable<String> getPasswordVar() {
		return password;
	}

	public LiveVariable<StoreCredentialsMode> getStoreVar() {
		return storeCredentials;
	}

	public LiveVariable<Boolean> getSkipSslVar() {
		return skipSslValidation;
	}

	public LiveVariable<String> getUrlVar() {
		return url;
	}

	public LiveVariable<CFSpace> getSpaceVar() {
		return space;
	}

	public LiveExpression<Boolean> getEnableSpacesUI() {
		return credentialsValidator.apply((r) -> r.isOk());
	}

	public LiveExpression<Boolean> getEnableUserName() {
		return method.apply((method) -> method==LoginMethod.PASSWORD);
	}

	public LiveExpression<ValidationResult> getSpaceValidator() {
		return spaceValidator;
	}

	public LiveExpression<ValidationResult> getResolvedSpacesValidator() {
		return resolvedSpacesValidator;
	}

	public LiveExpression<ValidationResult> getCredentialsValidator() {
		return credentialsValidator;
	}

	public void setMethod(LoginMethod v) {
		method.setValue(v);
	}

	public Validator getStoreCredentialsValidator() {
		return storeCredentialsValidator;
	}

}
