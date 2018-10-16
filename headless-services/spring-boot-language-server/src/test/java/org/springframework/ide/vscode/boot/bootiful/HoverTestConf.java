package org.springframework.ide.vscode.boot.bootiful;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;

@Configuration
public class HoverTestConf {

	@Bean PropertyIndexHarness indexHarness() {
		return new PropertyIndexHarness();
	}

	@Bean MockRunningAppProvider mockAppsHarness() {
		return new MockRunningAppProvider();
	}

	@Bean BootLanguageServerHarness harness(SimpleLanguageServer server, BootLanguageServerParams serverParams, PropertyIndexHarness indexHarness, JavaProjectFinder projectFinder) throws Exception {
		return new BootLanguageServerHarness(server, serverParams, indexHarness, projectFinder, LanguageId.JAVA, ".java");
	}

	@Bean Duration watchDogInterval() {
		return Duration.ofMillis(100);
	}

	@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server) {
		BootLanguageServerParams testDefaults = BootLanguageServerParams.createTestDefault(server);
		return new BootLanguageServerParams(
				indexHarness().getProjectFinder(),
				testDefaults.projectObserver,
				indexHarness().getIndexProvider(),
				indexHarness().getAdHocIndexProvider(),
				testDefaults.typeUtilProvider,
				mockAppsHarness().provider,
				watchDogInterval()
		);
	}

	@Bean JavaProjectFinder projectFinder(BootLanguageServerParams serverParams) {
		return serverParams.projectFinder;
	}
}