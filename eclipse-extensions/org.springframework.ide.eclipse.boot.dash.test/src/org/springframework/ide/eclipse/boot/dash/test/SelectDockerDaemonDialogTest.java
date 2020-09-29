package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog.Model;

public class SelectDockerDaemonDialogTest {

	private static final String DEFAULT_DOCKER_URL = "unix:///var/run/docker.sock";

	@Test
	public void initialValues() throws Exception {
		Model model = new SelectDockerDaemonDialog.Model();
		assertEquals(false, model.okPressed.getValue());
		assertEquals(true, model.useLocalDaemon.getValue());
		assertEquals(false, model.daemonUrlEnabled.getValue());
		assertEquals(DEFAULT_DOCKER_URL, model.daemonUrl.getValue());
	}

	@Test
	public void customUrlWidgets() throws Exception {
		Model model = new SelectDockerDaemonDialog.Model();
		assertEquals(true, model.useLocalDaemon.getValue());
		assertEquals(false, model.daemonUrlEnabled.getValue());
		assertEquals(DEFAULT_DOCKER_URL, model.daemonUrl.getValue());

		model.useLocalDaemon.setValue(false);
		assertEquals(true, model.daemonUrlEnabled.getValue());
		model.daemonUrl.setValue("custom");
		assertEquals("custom", model.daemonUrl.getValue());
		model.useLocalDaemon.setValue(true);
		assertEquals(false, model.daemonUrlEnabled.getValue());
		assertEquals(DEFAULT_DOCKER_URL, model.daemonUrl.getValue());
	}

}
