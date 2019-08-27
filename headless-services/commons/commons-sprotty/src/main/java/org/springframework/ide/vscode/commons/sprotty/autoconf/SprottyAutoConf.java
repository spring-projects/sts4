package org.springframework.ide.vscode.commons.sprotty.autoconf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.sprotty.scan.DiagramWebsocketServer;

@Configuration
@ComponentScan(basePackageClasses = DiagramWebsocketServer.class)
public class SprottyAutoConf {
	
}
