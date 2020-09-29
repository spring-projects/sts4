package org.springframework.ide.eclipse.boot.dash.api;

public interface DevtoolsConnectable {
	String getDevtoolsSecret();
	boolean hasDevtoolsDependency();

	default TemporalBoolean isDevtoolsConnectable() {
		return TemporalBoolean.now(hasDevtoolsDependency() && getDevtoolsSecret()!=null);
	}
}
