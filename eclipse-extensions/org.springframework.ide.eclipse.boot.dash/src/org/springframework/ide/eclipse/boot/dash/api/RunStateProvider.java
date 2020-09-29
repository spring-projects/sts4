package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.dash.model.RunState;

public interface RunStateProvider {

	RunState fetchRunState();

}
