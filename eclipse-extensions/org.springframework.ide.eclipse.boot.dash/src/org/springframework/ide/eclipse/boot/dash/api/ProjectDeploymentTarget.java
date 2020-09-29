package org.springframework.ide.eclipse.boot.dash.api;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public interface ProjectDeploymentTarget /* extends RunTarget */ {
	void performDeployment(Set<IProject> of, RunState runOrDebug) throws Exception;
}
