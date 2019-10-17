/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
import {CommandContribution, CommandRegistry} from '@theia/core';
import {QuickPickService} from '@theia/core/lib/common/quick-pick-service';
import {injectable, inject} from 'inversify';

interface ProcessCommandInfo {
    processKey : string;
    label: string;
    action: string
}

@injectable()
export class LiveDataCommandContribution implements CommandContribution {

    constructor(
        @inject(QuickPickService) private readonly quickPickService: QuickPickService
    ) {}

    registerCommands(commands: CommandRegistry): void {
        commands.registerCommand({
            id: 'theia-spring-boot.live-hover.connect',
            label: 'Manage Live Spring Boot Process Connections'
        }, {
            execute: () => this.liveHoverConnectHandler(commands, this.quickPickService)
        });
    }

    private async liveHoverConnectHandler(commands: CommandRegistry, quickPickService: QuickPickService) {
        const processData : ProcessCommandInfo[] = await commands.executeCommand('sts/livedata/listProcesses');
        const choiceMap = new Map<string, ProcessCommandInfo>();
        const choices : string[] = [];
        processData.forEach(p => {
            const slash = p.action.lastIndexOf('/');
            if (slash>=0) {
                var actionLabel = p.action.substring(slash+1);
                actionLabel = actionLabel.substring(0, 1).toUpperCase() + actionLabel.substring(1);
                const choiceLabel = actionLabel + " " + p.label;
                choiceMap.set(choiceLabel, p);
                choices.push(choiceLabel);
            }
        });
        if (choices) {
            const picked = await quickPickService.show(choices);
            if (picked) {
                const chosen = choiceMap.get(picked);
                await commands.executeCommand(chosen.action, chosen);
            }
        }
    }
}

