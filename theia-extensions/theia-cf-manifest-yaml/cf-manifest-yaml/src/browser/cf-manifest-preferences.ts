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

export const CfManifestYamlConfigSchema: PreferenceSchema = {
    'type': 'object',
    'title': 'CF Manifest YAML Configuration',
    properties: {
        'manifest-yaml.ls.javahome': {
            type: 'string',
            default: null,
            description: "Java Home folder to start CF Manifest YAML LS"
        },
        'manifest-yaml.ls.vmargs': {
            type: 'string',
            default: null,
            description: "Java VM arguments to start CF Manifest YAML LS"
        }
    }
};

export interface CfManifestYamlConfiguration {
    'manifest-yaml.ls.javahome': string;
    'manifest-yaml.ls.vmargs': string;
}

export const CfManifestYamlPreferences = Symbol('CfManifestYamlPreferences');
export type CfManifestYamlPreferences = PreferenceProxy<CfManifestYamlConfiguration>;

export function createCfManifestYamlPreferences(preferences: PreferenceService): CfManifestYamlPreferences {
    return createPreferenceProxy(preferences, CfManifestYamlConfigSchema);
}

export function bindCfManifestYamlPreferences(bind: interfaces.Bind): void {
    bind(CfManifestYamlPreferences).toDynamicValue(ctx => {
        const preferences = ctx.container.get<PreferenceService>(PreferenceService);
        return createCfManifestYamlPreferences(preferences);
    });
    bind(PreferenceContribution).toConstantValue({ schema: CfManifestYamlConfigSchema });
}
