/*
 * Copyright 2024 the original author or authors.
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
package org.openrewrite.staticanalysis;

import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.FindAnnotations;
import org.openrewrite.java.search.UsesJavaVersion;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

import java.time.Duration;
import java.util.Comparator;

public class AddSerialAnnotationToSerialVersionUID extends Recipe {
    @Override
    public String getDisplayName() {
        return "Add `@Serial` annotation to `serialVersionUID`";
    }

    @Override
    public String getDescription() {
        return "Annotation any `serialVersionUID` fields with `@Serial` to indicate it's part of the serialization mechanism.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    @NonNull
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.and(
                        new UsesJavaVersion<>(14),
                        new UsesType<>("java.io.Serializable", true)
                ),
                new JavaIsoVisitor<ExecutionContext>() {
                    @Override
                    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                        if (TypeUtils.isAssignableTo("java.io.Serializable", classDecl.getType())) {
                            return super.visitClassDeclaration(classDecl, ctx);
                        }
                        return classDecl;
                    }

                    @Override
                    public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext ctx) {
                        J.VariableDeclarations vd = super.visitVariableDeclarations(multiVariable, ctx);
                        if (isPrivateStaticFinalLongSerialVersionUID(vd) &&
                            FindAnnotations.find(vd, "@java.io.Serial").isEmpty()) {
                            maybeAddImport("java.io.Serial");
                            return JavaTemplate.builder("@Serial")
                                    .imports("java.io.Serial")
                                    .build()
                                    .apply(getCursor(), vd.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                        }
                        return vd;
                    }

                    private boolean isPrivateStaticFinalLongSerialVersionUID(J.VariableDeclarations vd) {
                        return vd.hasModifier(J.Modifier.Type.Private) &&
                               vd.hasModifier(J.Modifier.Type.Static) &&
                               vd.hasModifier(J.Modifier.Type.Final) &&
                               TypeUtils.asPrimitive(vd.getType()) == JavaType.Primitive.Long &&
                               vd.getVariables().size() == 1 &&
                               "serialVersionUID".equals(vd.getVariables().get(0).getSimpleName());
                    }
                }
        );
    }
}
