package org.springframework.ide.vscode.commons.sprotty.autoconf;

import org.eclipse.sprotty.IPopupModelFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.sprotty.scan.DiagramWebsocketServer;

@Configuration
@ComponentScan(basePackageClasses = DiagramWebsocketServer.class)
public class SprottyAutoConf {
	
	@Bean
	@ConditionalOnMissingBean(IPopupModelFactory.class)
	public IPopupModelFactory popupModelFactory() {
		return new IPopupModelFactory.NullImpl();
	}

}
