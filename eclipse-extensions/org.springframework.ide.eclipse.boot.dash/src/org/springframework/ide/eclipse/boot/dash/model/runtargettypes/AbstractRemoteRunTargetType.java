package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;

public abstract class AbstractRemoteRunTargetType<Params> extends AbstractRunTargetType<Params> implements RemoteRunTargetType<Params> {

	public AbstractRemoteRunTargetType(SimpleDIContext injections, String name) {
		super(injections, name);
	}

	@Override
	public final boolean canInstantiate() {
		return true;
	}
}
