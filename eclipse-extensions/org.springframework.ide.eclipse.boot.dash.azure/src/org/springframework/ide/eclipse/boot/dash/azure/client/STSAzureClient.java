package org.springframework.ide.eclipse.boot.dash.azure.client;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.azure.runtarget.AzureTargetParams;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.auth.AzureAuthHelper;
import com.microsoft.azure.auth.AzureCredential;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.azure.management.appplatform.v2019_05_01_preview.implementation.ServiceResourceInner;
import com.microsoft.azure.management.resources.Subscription;

public class STSAzureClient {

	/**
	 * Informations identifying a user and their credentials. These are not used directly
	 * to access the api. Instead they are used to obtain 'authTokens'.
	 */
	private AzureCredential credentials;

	/**
	 * Credentials are used to obtain oauthTokens:
	 */
	private AzureTokenCredentials authTokens;

    /**
     * The azure client for get list of subscriptions (i.e. this client is not targetted to
     * a specific spring cloud cluster (called a 'service' in Azure speak. This is the equivalent
     * of a CF space (i.e. a place in which apps can be deployed). So this client is
     * authenticated to a specific user but not targeted to any particular subscription/group/service.
     */
    private Authenticated azure;

    protected SpringServiceClient springServiceClient;

    /**
     * Set to a specific subscription once it has been selected. Set to null otherwise.
     */
    private Subscription subscription;

	private ServiceResourceInner cluster;

	public void authenticate() throws Exception {
        getAuthTokens();
        this.azure = Azure.authenticate(authTokens);
        if (azure==null) {
        	throw new IOException("Couldn't obtain credentials");
        }
	}

	private void getAuthTokens() throws Exception {
		final AzureEnvironment environment = AzureEnvironment.AZURE;
//        try {
		if (this.credentials==null) {
            this.credentials = AzureAuthHelper.oAuthLogin(environment);
		}

         // We can't use the deviceLogin helper as is because it prints to sysout to interact with the user
         // and instructing them to get paste a auth code into a browser.
//        } catch (DesktopNotSupportedException e) {
//            this.credentials = AzureAuthHelper.deviceLogin(environment);
//        }
        authTokens = AzureAuthHelper.getMavenAzureLoginCredentials(credentials, environment);
	}

	public SpringServiceClient getSpringServiceClient() {
		if (springServiceClient == null) {
			springServiceClient = new SpringServiceClient(authTokens, subscription.subscriptionId(), getUserAgent());
		}
		return springServiceClient;
	}

    private String getUserAgent() {
    	return "spring-tool-suite/4.1.5-testing";
	}


//	public STSAzureClient connect(UserInteractions ui) throws Exception {
//		authenticate();
//		selectSub(ui);
//		selectAppCluster();
//
//		return this;
//	}

//	private void selectSub(UserInteractions ui) {
//		PagedList<Subscription> subs = azure.subscriptions().list();
//		if (subs.isEmpty()) {
//			ui.errorPopup("No Azure Subscriptions Found", "You need an Azure Subscription, but none was "
//					+ "found to be associated with your user. Please sign up for an Azure subscription or "
//					+ "try again to login as a different user");
//			return;
//		}
//
//		subs.forEach(subscription -> {
//			System.out.println("Subscription: "+subscription.displayName());
//			System.out.println("          Id: "+subscription.subscriptionId());
//			azure.withSubscription(subscription.subscriptionId());
//			System.out.println(subscription);
//		});
//		//TODO:
////		if (subs.size()>1) {
////			 choose one
////		} else {
//		this.sub = subs.get(0);
////	    }
//	}

	/**
	 * Connect to azure with credentials obtained by prompting the user
	 */
	static public boolean login(UserInteractions ui) {
		try {
			if (ui.confirmOperation("Obtaining Credentials via OAuth",
					"To access your Azure Spring Cloud subscriptions, resources and services, " +
					"STS needs to be authorized by you. A web browser will now be opened for that purpose.")
			) {
				STSAzureClient client = new STSAzureClient();
				client.authenticate();
				return true;
			}
		} catch (Exception e) {
			Log.log(e);
			ui.errorPopup("Authentication failed", ExceptionUtil.getMessage(e));
		}
		return false;
	}

	public PagedList<Subscription> getSubsriptions() {
		Assert.isLegal(azure!=null, "Not authenticated (call login or connect befor calling this method)");
		return azure.subscriptions().list();
	}

	public void setSubscription(Subscription sub) {
		this.subscription = sub;
	}

	public void setCluster(ServiceResourceInner cluster) {
		this.cluster = cluster;
	}

	public AzureTargetParams getTargetParams() {
		return new AzureTargetParams(
				this.credentials,
				subscription.subscriptionId(),
				subscription.displayName(),
				cluster.id(),
				cluster.name()
		);
	}

	public void reconnect(AzureTargetParams params) throws Exception {
		this.credentials = params.getCredentials();
		this.authenticate();
		this.setSubscription(azure.subscriptions().getById(params.getSubscriptionId()));
		this.setCluster(getSpringServiceClient().getClusterById(params.getClusterId()));
	}

	private Subscription getSubsription(String subscriptionId) {
		return azure.subscriptions().getById(subscriptionId);
	}

}