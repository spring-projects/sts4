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

export const HIGHLIGHTS_PREF_NAME = 'spring-boot.boot-hints.on';
export const XML_SUPPORT_PREF_NAME = 'spring-boot.support-spring-xml-config.on';
export const CODELENS_PREF_NAME = 'spring-boot.highlight-codelens.on';

export const BootConfigSchema: PreferenceSchema = {
    'type': 'object',
    'title': 'Spring Boot Configuration',
    properties: {
        'spring-boot.boot-hints.on': {
            type: 'boolean',
            description: 'Enable/Disable Spring running Boot application live hints decorators in Java source code.',
            default: true
        },
        'spring-boot.support-spring-xml-config.on': {
            type: 'boolean',
            description: 'Enable/Disable Support for Spring XML Config files',
            default: false
        },
        'spring-boot.change-detection.on': {
            type: 'boolean',
            description: 'Enable/Disable detecting changes of running Spring Boot applications.',
            default: false
        },
        'spring-boot.highlight-codelens.on': {
            type: 'boolean',
            default: true,
            description: 'Enable/Disable Spring running Boot application Code Lenses'
        },
        'spring-boot.ls.javahome': {
            type: 'string',
            default: null,
            description: "Java Home folder to start Spring Boot LS"
        },
        'spring-boot.ls.vmargs': {
            type: 'string',
            default: null,
            description: "Java VM arguments to start Spring Boot LS"
        }
    }
};

export interface BootConfiguration {
    'spring-boot.boot-hints.on': boolean;
    'spring-boot.support-spring-xml-config.on': boolean;
    'spring-boot.change-detection.on': boolean;
    'spring-boot.highlight-codelens.on': boolean;
    'spring-boot.ls.javahome': string;
    'spring-boot.ls.vmargs': string;
}

export const BootPreferences = Symbol('BootPreferences');
export type BootPreferences = PreferenceProxy<BootConfiguration>;

export function createBootPreferences(preferences: PreferenceService): BootPreferences {
    return createPreferenceProxy(preferences, BootConfigSchema);
}

export function bindBootPreferences(bind: interfaces.Bind): void {
    bind(BootPreferences).toDynamicValue(ctx => {
        const preferences = ctx.container.get<PreferenceService>(PreferenceService);
        return createBootPreferences(preferences);
    });
    bind(PreferenceContribution).toConstantValue({ schema: BootConfigSchema });
}
