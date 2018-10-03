package org.springframework.ide.vscode.languageserver.starter;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.languageserver.LanguageServerRunner;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration
public class LanguageServerAutoconf {
	
	@Bean public LanguageServerRunner serverApp(
			@Qualifier("serverName") String serverName, 
			Provider<SimpleLanguageServer> languageServerFactory
	) {
		return new LanguageServerRunner(serverName, languageServerFactory);
	}
	
}
