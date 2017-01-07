/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

/**
 * Extrat bits of info that are 'prefetched' asynchronously to fill
 * out the CFApplication with info that C2 client doesn't initially return.
 *
 * @author Kris De Volder
 */
public interface ApplicationExtras {
	Mono<Map<String,String>> getEnv();
	Mono<List<String>> getServices();
	Mono<String> getBuildpack();
	Mono<String> getStack();
	Mono<Integer> getTimeout();
	Mono<String> getCommand();
	Mono<String> getHealthCheckType();
}
