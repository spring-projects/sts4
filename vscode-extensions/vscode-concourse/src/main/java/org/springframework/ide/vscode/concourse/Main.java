package org.springframework.ide.vscode.concourse;

import java.io.IOException;

import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class Main {
   	SimpleLanguageServer server = new ConcourseLanguageServer();
   	
   	public static void main(String[] args) throws IOException {
		LaunguageServerApp.start(ConcourseLanguageServer::new);
	}
}
