package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.api.App;
import org.springframework.ide.eclipse.boot.dash.api.AppContext;
import org.springframework.ide.eclipse.boot.dash.api.RunStateProvider;
import org.springframework.ide.eclipse.boot.dash.azure.client.SpringServiceClient;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.AppResource;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResource;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.DeploymentResourceProvisioningState;

public class AzureApp implements App, RunStateProvider {

	public static final Set<DeploymentResourceProvisioningState> BUSY = ImmutableSet.of(
			DeploymentResourceProvisioningState.CREATING,
			DeploymentResourceProvisioningState.UPDATING
	);

	private AzureRunTarget target;
	private AppResource app;

	public AzureApp(AzureRunTarget azureRunTarget, AppResource appResource) {
		this.target = azureRunTarget;
		this.app = appResource;
	}

	@Override
	public String getName() {
		return app.name();
	}

	@Override
	public RunState fetchRunState() {
		String rg = target.getResourceGroupName();
		String sn = target.getClusterName();
		SpringServiceClient client = target.getClient();
		if (client!=null) {
			String activeDepName = app.properties().activeDeploymentName();
			if (activeDepName!=null) {
				DeploymentResource dep = client.getSpringManager().deployments().getAsync(rg, sn, app.name(), activeDepName).toBlocking().single();
				if (dep!=null) {
					DeploymentResourceProvisioningState state = dep.properties().provisioningState();
					if (BUSY.contains(state)) {
						return RunState.STARTING;
					} else if (DeploymentResourceProvisioningState.SUCCEEDED.equals(state)) {
						if (dep.properties().active()) {
							return RunState.RUNNING;
						}
					} else if (DeploymentResourceProvisioningState.FAILED.equals(state)) {
						return RunState.CRASHED;
					} else {
						return RunState.UNKNOWN;
					}
				}
			}
			return RunState.INACTIVE;
		}
		return RunState.UNKNOWN;
	}

	@Override
	public AzureRunTarget getTarget() {
		return target;
	}

	@Override
	public void setContext(AppContext context) {
		// TODO Auto-generated method stub
		
	}
}
