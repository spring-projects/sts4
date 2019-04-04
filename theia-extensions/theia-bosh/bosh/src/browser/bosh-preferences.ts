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
import { interfaces } from 'inversify';
import { createPreferenceProxy, PreferenceProxy, PreferenceService, PreferenceContribution, PreferenceSchema } from '@theia/core/lib/browser';

// tslint:disable:max-line-length

export const BoshConfigSchema: PreferenceSchema = {
    'type': 'object',
    'title': 'Bosh CLI Configuration',
    properties: {
        'boot-bosh.cli.command': {
            type: 'string',
            description: 'Path to an executable to launch the bosh cli V2. A V2 cli is required! Set this to null to completely disable all editor features that require access to the bosh director.',
            default: 'bosh'
        },
        'bosh.cli.target': {
            type: 'string',
            description: `Specifies the director/environment to target when executing bosh cli commands. I.e. this value is passed to the CLI via \`-e\` parameter.`,
            default: null
        },
        'bosh.cli.timeout': {
            type: 'integer',
            description: `Number of seconds before CLI commands are terminated with a timeout.`,
            default: 3
        },
        'bosh-yaml.ls.javahome': {
            type: 'string',
            default: null,
            description: "Java Home folder to start Bosh YAML LS"
        },
        'bosh-yaml.ls.vmargs': {
            type: 'string',
            default: null,
            description: "Java VM arguments to start Bosh YAML LS"
        }
    }
};

export interface BoshConfiguration {
    'boot-bosh.cli.command': string | null;
    'bosh.cli.target': string | null;
    'bosh.cli.timeout': number;
    'bosh-yaml.ls.javahome': string;
    'bosh-yaml.ls.vmargs': string;
}

export const BoshPreferences = Symbol('BoshPreferences');
export type BoshPreferences = PreferenceProxy<BoshConfiguration>;

export function createBootPreferences(preferences: PreferenceService): BoshPreferences {
    return createPreferenceProxy(preferences, BoshConfigSchema);
}

export function bindBoshPreferences(bind: interfaces.Bind): void {
    bind(BoshPreferences).toDynamicValue(ctx => {
        const preferences = ctx.container.get<PreferenceService>(PreferenceService);
        return createBootPreferences(preferences);
    });
    bind(PreferenceContribution).toConstantValue({ schema: BoshConfigSchema });
}
