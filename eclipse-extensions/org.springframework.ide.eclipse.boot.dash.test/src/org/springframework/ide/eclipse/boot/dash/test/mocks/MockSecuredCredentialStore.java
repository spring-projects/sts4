package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;

public class MockSecuredCredentialStore implements SecuredCredentialsStore {

	private Map<String,String> store = new HashMap<>();
	private boolean isUnlocked = false;

	@Override
	public synchronized String getCredentials(String runTargetId) {
		isUnlocked = true;
		return store.get(runTargetId);
	}

	@Override
	public synchronized void setCredentials(String runTargetId, String password) {
		isUnlocked = true;
		store.put(runTargetId, password);
	}

	@Override
	public boolean isUnlocked() {
		return isUnlocked;
	}

}
