/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh.models;

import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.yaml.path.YamlNavigable;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.util.Streams;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Allows using {@link YamlPath} / {@link YamlNavigable} on {@link JsonNode}s
 */
public class JSONCursor implements YamlNavigable<JSONCursor> {

	public final JsonNode target;

	public JSONCursor(JsonNode target) {
		super();
		this.target = target;
	}

	@Override
	public Stream<JSONCursor> traverseAmbiguously(YamlPathSegment s) {
		return oneStep(s).map(JSONCursor::new);
	}

	private Stream<JsonNode> oneStep(YamlPathSegment s) {
		if (target==null) {
			return Stream.empty();
		}
		switch (s.getType()) {
			case KEY_AT_KEY: {
				return Streams.fromNullable(target.get(s.toPropString()));
			}
			case ANY_CHILD: {
				return Streams.fromIterable(target);
			}
			case VAL_AT_INDEX: {
				return Streams.fromNullable(target.get(s.toIndex()));
			}
			case VAL_AT_KEY: {
				return Streams.fromNullable(target.get(s.toPropString()));
			}
			default:
				throw new IllegalStateException("Missing case for "+s.getType());
		}
	}

	@Override
	public String toString() {
		return ""+target;
	}

}
