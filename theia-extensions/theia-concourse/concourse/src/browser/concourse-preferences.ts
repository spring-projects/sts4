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

export const ConcourseYamlConfigSchema: PreferenceSchema = {
    'type': 'object',
    'title': 'Concourse YAML Configuration',
    properties: {
        'concourse-yaml.ls.javahome': {
            type: 'string',
            default: null,
            description: "Java Home folder to start Concourse YAML LS"
        },
        'concourse-yaml.ls.vmargs': {
            type: 'string',
            default: null,
            description: "Java VM arguments to start Concourse YAML LS"
        }
    }
};

export interface ConcourseYamlConfiguration {
    'concourse-yaml.ls.javahome': string;
    'concourse-yaml.ls.vmargs': string;
}

export const ConcourseYamlPreferences = Symbol('ConcourseYamlPreferences');
export type ConcourseYamlPreferences = PreferenceProxy<ConcourseYamlConfiguration>;

export function createConcourseYamlPreferences(preferences: PreferenceService): ConcourseYamlPreferences {
    return createPreferenceProxy(preferences, ConcourseYamlConfigSchema);
}

export function bindConcourseYamlPreferences(bind: interfaces.Bind): void {
    bind(ConcourseYamlPreferences).toDynamicValue(ctx => {
        const preferences = ctx.container.get<PreferenceService>(PreferenceService);
        return createConcourseYamlPreferences(preferences);
    });
    bind(PreferenceContribution).toConstantValue({ schema: ConcourseYamlConfigSchema });
}
