package org.springframework.ide.eclipse.boot.dash.model.remote;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.api.App;

public interface ChildBearing {

	List<App> fetchChildren() throws Exception;

}
