/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
export const JAVA_PROPERTIES_LANGUAGE_ID = 'java-properties';
export const JAVA_PROPERTIES_LANGUAGE_GRAMMAR_SCOPE = 'source.java-properties';

export const YAML_LANGUAGE_ID = 'yaml';
export const YAML_LANGUAGE_GRAMMAR_SCOPE = 'source.yaml';

// Java Properties file format config and grammar

export const JAVA_PROPERTIES_CONFIG: monaco.languages.LanguageConfiguration = {
    comments: {
        lineComment: "#"
    }
};

export const JAVA_PROPERTIES_TM_GRAMMAR = require('../../data/java-properties.tmLanguage.json');


// YAML file format config and grammar

export const YAML_CONFIG: monaco.languages.LanguageConfiguration = {
    comments: {
        lineComment: "#"
    },
    brackets: [
        ["{", "}"],
        ["[", "]"],
        ["(", ")"]
    ],
    autoClosingPairs: [
        { open: "{", close: "}" },
        { open: "[", close: "]" },
        { open: "(", close: ")" },
        { open: "\"", close: "\"" },
        { open: "'", close: "'" }
    ],
    surroundingPairs: [
        { open: "{", close: "}" },
        { open: "[", close: "]" },
        { open: "(", close: ")" },
        { open: "\"", close: "\"" },
        { open: "'", close: "'" }
    ],
    indentationRules: {
        increaseIndentPattern: new RegExp("^\\s*.*(:|-) ?(&amp;\\w+)?(\\{[^}\"']*|\\([^)\"']*)?$"),
        decreaseIndentPattern: new RegExp("^\\s+\\}$")
    }
};

export const YAML_TM_GRAMMAR = require('../../data/yaml.tmLanguage.json');
