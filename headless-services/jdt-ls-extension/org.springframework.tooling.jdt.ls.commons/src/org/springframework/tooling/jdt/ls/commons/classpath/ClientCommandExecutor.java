package org.springframework.tooling.jdt.ls.commons.classpath;

public interface ClientCommandExecutor {
	Object executeClientCommand(String id, Object... params);
}
