package org.springframework.ide.vscode.commons.sprotty.api;

import org.eclipse.sprotty.RequestModelAction;
import org.eclipse.sprotty.SModelRoot;

public interface DiagramGenerator {
	SModelRoot generateModel(String clientId, RequestModelAction modelRequest);
}
