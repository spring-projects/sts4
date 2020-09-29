package org.springframework.ide.eclipse.boot.dash.console;

import reactor.core.Disposable;

public interface LegacyLogSource {
	Disposable connectLog(ApplicationLogConsole logConsole);
}
