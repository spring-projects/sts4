package org.springframework.ide.vscode.manifest.yaml;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

@Configuration
@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
public class DefaultCloudfoundryConfig {

	@Bean public CloudFoundryClientFactory cloudfoundryClientFactory() {
		return DefaultCloudFoundryClientFactoryV2.INSTANCE;
	}

	@Bean public ClientParamsProvider cloudfoundryClientParamsProvider() {
		return CfCliParamsProvider.getInstance();
	}
}
