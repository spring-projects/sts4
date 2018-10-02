package org.springframework.ide.vscode.languageserver.starter;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration
public class LanguageServerAutoconf {
	
	@Bean public LaunguageServerApp serverApp(
			@Qualifier("serverName") String serverName, 
			Provider<SimpleLanguageServer> languageServerFactory
	) {
		return new LaunguageServerApp(serverName, languageServerFactory);
	}
	
	@Bean public CommandLineRunner serverStarter(LaunguageServerApp serverApp) {
		return args -> {
			serverApp.startAsync();
		};
	}
	
}
