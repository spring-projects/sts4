/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;


public class ClientFactory {

	private Config fabric8 = Config.autoConfigure(null);

	public Config getFabric8() {
		return this.fabric8;
	}

	public KubernetesClient getKubernetesClient(String nameSpace) {
		Config config = getFabric8();
		config.setNamespace(nameSpace);
		return new DefaultKubernetesClient(config);
	}

}
