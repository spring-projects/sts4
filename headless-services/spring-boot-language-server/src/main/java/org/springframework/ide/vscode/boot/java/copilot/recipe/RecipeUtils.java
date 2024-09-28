/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.vscode.boot.java.copilot.recipe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.openrewrite.Result;
import org.springframework.ide.vscode.boot.java.copilot.SpringCliException;

/**
 * Utilities for recipe execution
 */
public final class RecipeUtils {

	private RecipeUtils() {
	}

	public static void writeResults(String recipeName, Path path, List<Result> resultList) {
		try {
			for (Result result : resultList) {
				try (BufferedWriter sourceFileWriter = Files.newBufferedWriter(path)) {
					sourceFileWriter.write(result.getAfter().printAllTrimmed());
				}
			}
		}
		catch (IOException ex) {
			throw new SpringCliException("Could not write recipe results to path = " + path, ex);
		}
	}

}
