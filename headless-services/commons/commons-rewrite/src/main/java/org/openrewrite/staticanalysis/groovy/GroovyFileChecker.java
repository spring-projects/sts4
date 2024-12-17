/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.staticanalysis.groovy;

import org.jspecify.annotations.Nullable;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.groovy.tree.G;
import org.openrewrite.marker.SearchResult;

/**
 * Add a search marker if vising a Groovy file
 */
public class GroovyFileChecker<P> extends TreeVisitor<Tree, P> {
    @Override
    public @Nullable Tree visit(@Nullable Tree tree, P p) {
        if (tree instanceof G.CompilationUnit) {
            return SearchResult.found(tree);
        }
        return tree;
    }
}
