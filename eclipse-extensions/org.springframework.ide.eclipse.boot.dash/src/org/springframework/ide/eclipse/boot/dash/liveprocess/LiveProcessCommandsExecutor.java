/*******************************************************************************
 * Copyright (c) 2019, 2023 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.liveprocess;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LiveProcessCommandsExecutor {

	Flux<CommandInfo> listCommands();
	Mono<Void> executeCommand(CommandInfo cmd);

	static LiveProcessCommandsExecutor getDefault() {
		return new DefaultLiveProcessCommandExecutor();
	}
}
