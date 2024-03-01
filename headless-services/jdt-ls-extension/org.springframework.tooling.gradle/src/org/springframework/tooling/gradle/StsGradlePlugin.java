package org.springframework.tooling.gradle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class StsGradlePlugin extends Plugin {
	
	private static StsGradlePlugin plugin;
	
	static StsGradlePlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
