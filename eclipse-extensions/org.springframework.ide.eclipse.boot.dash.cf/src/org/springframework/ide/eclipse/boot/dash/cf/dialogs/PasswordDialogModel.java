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
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials.LoginMethod;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import reactor.core.publisher.Mono;

/**
 * Password dialog model. Provides ability to specify password and whether it
 * needs to be stored.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class PasswordDialogModel implements OkButtonHandler {

	private static final ValidationResult REQUEST_VALIDATION_MESSAGE = ValidationResult.info(
			"Click the 'Validate' button to verify the credentials.");

	private static final ValidationResult VALIDATION_IN_PROGRESS_MESSAGE = ValidationResult.info(
			"Please wait. Contacting CF to verify the credentials..."
	);

	private CloudFoundryTargetProperties currentParams;
	private CloudFoundryClientFactory clientFactory;

	private final LiveVariable<String> refreshToken = new LiveVariable<>(); //This is set when credentials are succesfully validated.
	private final LiveVariable<Boolean> needValidationRequest = new LiveVariable<>(true);

	private LiveVariable<ValidationResult> credentialsValidationResult;
	final private LiveVariable<LoginMethod> fMethod;
	final private LiveVariable<String> fPasswordVar;
	final private LiveVariable<StoreCredentialsMode> fStoreVar;
	private boolean okButtonPressed = false;
	private Validator passwordValidator;
	private LiveExpression<ValidationResult> storeValidator;

	private <T> void credentialsChangedHandler(LiveExpression<T> exp, T value) {
		needValidationRequest.setValue(true);
	}

	public PasswordDialogModel(CloudFoundryClientFactory cfFactory, CloudFoundryTargetProperties currentParams, StoreCredentialsMode storeMode) {
		super();
		this.clientFactory = cfFactory;
		this.currentParams = currentParams;
		fPasswordVar = new LiveVariable<>("");
		fStoreVar = new LiveVariable<>(storeMode);
		fMethod = new LiveVariable<>(LoginMethod.PASSWORD);
		storeValidator = makeStoreCredentialsValidator(getMethodVar(), fStoreVar);
		credentialsValidationResult = new LiveVariable<>(REQUEST_VALIDATION_MESSAGE);
		fMethod.addListener(this::credentialsChangedHandler);
		fPasswordVar.addListener(this::credentialsChangedHandler);
	}

	public String getUser() {
		return currentParams.getUsername();
	}

	public String getTargetId() {
		return CloudFoundryTargetProperties.getId(currentParams);
	}

	public LiveVariable<String> getPasswordVar() {
		return fPasswordVar;
	}

	public LiveVariable<StoreCredentialsMode> getStoreVar() {
		return fStoreVar;
	}

	public boolean isOk() {
		return okButtonPressed;
	}

	@Override
	public void performOk() throws Exception {
		okButtonPressed = true;
	}

	public LiveExpression<ValidationResult> getPasswordValidator() {
		if (passwordValidator==null) {
			passwordValidator = new Validator() {
				{
					dependsOn(fPasswordVar);
					dependsOn(needValidationRequest);
					dependsOn(credentialsValidationResult);
				}
				@Override
				protected ValidationResult compute() {
					String pw = fPasswordVar.getValue();
					if (!StringUtil.hasText(pw)) {
						return ValidationResult.error("Password can not be empty");
					}
					if (needValidationRequest.getValue()) {
						return REQUEST_VALIDATION_MESSAGE;
					}
					return credentialsValidationResult.getValue();
				}
			};
		}
		return passwordValidator;
	}

	public LiveExpression<ValidationResult> getStoreValidator() {
		return storeValidator;
	}

	/**
	 * Determines the 'effective' StoreCredentialsMode. This may be different
	 * from what the user explicitly chose. If the user choice is 'invalid'
	 * we ignore it (with a warning) and replace it with STORE_NOTHING.
	 */
	public StoreCredentialsMode getEffectiveStoreMode() {
		if (storeValidator.getValue().isOk()) {
			return fStoreVar.getValue();
		}
		return StoreCredentialsMode.STORE_NOTHING;
	}

	public static Validator makeStoreCredentialsValidator(LiveExpression<CFCredentials.LoginMethod> method, LiveExpression<StoreCredentialsMode> storeCredentials ) {
		return new Validator() {
			{
				dependsOn(method);
				dependsOn(storeCredentials);
			}

			@Override
			protected ValidationResult compute() {
				if (
					method.getValue()==CFCredentials.LoginMethod.TEMPORARY_CODE &&
					storeCredentials.getValue()==StoreCredentialsMode.STORE_PASSWORD
				) {
					return ValidationResult.warning("'Store Password' is useless for a 'Temporary Code'. This option will be ignored!");
				}
				return ValidationResult.OK;
			}
		};
	}

	/**
	 * Validates credentials currently entered in the dialog fields, and update
	 * other dialog model elements in the process (validation result / status and
	 * refreshToken needed to produce effective credential object.
	 */
	public Mono<ValidationResult> validateCredentials() {
		return validateCredentialsHelper(CFCredentials.fromLogin(this.fMethod.getValue(), this.fPasswordVar.getValue()))
		.doOnSubscribe((e) -> {
			needValidationRequest.setValue(false);
			refreshToken.setValue(null);
			credentialsValidationResult.setValue(VALIDATION_IN_PROGRESS_MESSAGE);
		})
		.onErrorResume((e) -> Mono.just(ValidationResult.error(ExceptionUtil.getMessage(e))))
		.doOnNext((result) -> {
			credentialsValidationResult.setValue(result);
		});
	}

	/**
	 * Validates a given credential object and returns a validation result (asynchornously).
	 */
	private Mono<ValidationResult> validateCredentialsHelper(CFCredentials creds) {

		return Mono.defer(() -> {
			if (!StringUtil.hasText(creds.getSecret())) {
				//Don't bother verifying empty passwords.
				return Mono.just(REQUEST_VALIDATION_MESSAGE);
			}
			CFClientParams params = new CFClientParams(
					currentParams.getUrl(),
					currentParams.getUsername(),
					creds,
					currentParams.isSelfsigned(),
					null, null,
					currentParams.skipSslValidation()
			);
			ClientRequests client = clientFactory.getClient(params);
			return client.getUserName()
			.flatMap(actualUserName -> {
				refreshToken.setValue(client.getRefreshToken());
				if (!currentParams.getUsername().equals(actualUserName)) {
					return Mono.just(ValidationResult.error("The credentials belong to a different user!"));
				}
				return Mono.just(ValidationResult.OK);
			})
			.doFinally(signal -> client.dispose());
		});
	}

	public LiveVariable<LoginMethod> getMethodVar() {
		return fMethod;
	}

	public CFCredentials getCredentials() {
		String refreshToken = this.refreshToken.getValue();
		if (refreshToken==null) {
			throw new IllegalStateException("Credentials must be validated before retrieving them from the model");
		}
		//Produce credential object consistent with store credentials mode
		StoreCredentialsMode storeMode = getEffectiveStoreMode();
		switch (storeMode) {
		case STORE_NOTHING:
		case STORE_TOKEN:
			return CFCredentials.fromRefreshToken(refreshToken);
		case STORE_PASSWORD:
			return CFCredentials.fromPassword(fPasswordVar.getValue());
		default:
			throw new IllegalStateException("Bug! Missing case?");
		}
	}

}
