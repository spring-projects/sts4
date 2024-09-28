package org.springframework.ide.vscode.boot.java.copilot;

import java.util.Map;

public interface TemplateEngine {

	String process(String template, Map context);

}
