/*
 * Copyright (C) 2018 TypeFox and others.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

import { interfaces } from 'inversify';
import { createPreferenceProxy, PreferenceProxy, PreferenceService, PreferenceContribution, PreferenceSchema } from '@theia/core/lib/browser';

// tslint:disable:max-line-length

export const BootConfigSchema: PreferenceSchema = {
    'type': 'object',
    'title': 'Spring Boot Java Configuration',
    properties: {
        'boot-java.boot-hints.on': {
            type: 'boolean',
            description: 'Enable/Disable Spring running Boot application live hints decorators in Java source code.',
            default: true
        },
        'spring-boot.ls.java.home': {
            type: 'string',
            description: `Override JAVA_HOME used for launching the spring-boot-language-server JVM process.`,
            default: null
        },
        'spring-boot.ls.java.heap': {
            type: 'string',
            description: `Max JVM heap value, passed via -Xmx argument when launching spring-boot-language-server JVM process.`,
            default: null
        }
    }
};

export interface BootConfiguration {
    'boot-java.boot-hints.on': boolean;
    'spring-boot.ls.java.home': string | null;
    'spring-boot.ls.java.heap': string | null;
}

export const BootPreferences = Symbol('BootJavaPreferences');
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
