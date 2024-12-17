/*
 * Copyright 2021 the original author or authors.
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

import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.VariableNameUtils;
import org.openrewrite.java.search.SemanticallyEqual;
import org.openrewrite.java.search.UsesJavaVersion;
import org.openrewrite.java.tree.*;
import org.openrewrite.java.tree.J.VariableDeclarations.NamedVariable;
import org.openrewrite.marker.Markers;
import org.openrewrite.staticanalysis.groovy.GroovyFileChecker;
import org.openrewrite.staticanalysis.kotlin.KotlinFileChecker;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.openrewrite.Tree.randomId;
import static org.openrewrite.java.VariableNameUtils.GenerationStrategy.INCREMENT_NUMBER;

public class InstanceOfPatternMatch extends Recipe {

    @Override
    public String getDisplayName() {
        return "Changes code to use Java 17's `instanceof` pattern matching";
    }

    @Override
    public String getDescription() {
        return "Adds pattern variables to `instanceof` expressions wherever the same (side effect free) expression is referenced in a corresponding type cast expression within the flow scope of the `instanceof`." +
               " Currently, this recipe supports `if` statements and ternary operator expressions.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(1);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        TreeVisitor<?, ExecutionContext> preconditions = Preconditions.and(
                new UsesJavaVersion<>(17),
                Preconditions.not(new KotlinFileChecker<>()),
                Preconditions.not(new GroovyFileChecker<>())
        );

        return Preconditions.check(preconditions, new JavaVisitor<ExecutionContext>() {
            @Override
            public @Nullable J postVisit(J tree, ExecutionContext ctx) {
                J result = super.postVisit(tree, ctx);
                InstanceOfPatternReplacements original = getCursor().getMessage("flowTypeScope");
                if (original != null && !original.isEmpty()) {
                    return UseInstanceOfPatternMatching.refactor(result, original, getCursor().getParentOrThrow());
                }
                return result;
            }

            @Override
            public J.InstanceOf visitInstanceOf(J.InstanceOf instanceOf, ExecutionContext ctx) {
                instanceOf = (J.InstanceOf) super.visitInstanceOf(instanceOf, ctx);
                if (instanceOf.getPattern() != null || !instanceOf.getSideEffects().isEmpty()) {
                    return instanceOf;
                }

                Cursor maybeReplacementRoot = null;
                J additionalContext = null;
                boolean flowScopeBreakEncountered = false;
                for (Iterator<Cursor> it = getCursor().getPathAsCursors(); it.hasNext(); ) {
                    Cursor next = it.next();
                    Object value = next.getValue();
                    if (value instanceof J.Binary) {
                        J.Binary binary = (J.Binary) value;
                        if (!flowScopeBreakEncountered && binary.getOperator() == J.Binary.Type.And) {
                            additionalContext = binary;
                        } else {
                            flowScopeBreakEncountered = true;
                        }
                    } else if (value instanceof J.Unary && ((J.Unary) value).getOperator() == J.Unary.Type.Not) {
                        // TODO this could be improved (the pattern variable may be applicable in the else case
                        // or even in subsequent statements (due to the flow scope semantics)
                        flowScopeBreakEncountered = true;
                    } else if (value instanceof Statement) {
                        maybeReplacementRoot = next;
                        break;
                    }
                }

                if (maybeReplacementRoot != null) {
                    J root = maybeReplacementRoot.getValue();
                    Set<J> contexts = new HashSet<>();
                    if (!flowScopeBreakEncountered) {
                        if (root instanceof J.If) {
                            contexts.add(((J.If) root).getThenPart());
                        } else if (root instanceof J.Ternary) {
                            contexts.add(((J.Ternary) root).getTruePart());
                        }
                    }
                    if (additionalContext != null) {
                        contexts.add(additionalContext);
                    }

                    if (!contexts.isEmpty()) {
                        InstanceOfPatternReplacements replacements = maybeReplacementRoot
                                .computeMessageIfAbsent("flowTypeScope", k -> new InstanceOfPatternReplacements(root));
                        replacements.registerInstanceOf(instanceOf, contexts);
                    }
                }
                return instanceOf;
            }

            @Override
            public J visitTypeCast(J.TypeCast typeCast, ExecutionContext ctx) {
                J result = super.visitTypeCast(typeCast, ctx);
                if (result instanceof J.TypeCast) {
                    InstanceOfPatternReplacements replacements = getCursor().getNearestMessage("flowTypeScope");
                    if (replacements != null) {
                        replacements.registerTypeCast((J.TypeCast) result, getCursor());
                    }
                }
                return result;
            }
        });
    }

    private static class ExpressionAndType {
        private final Expression expression;
        private final JavaType type;
		public ExpressionAndType(Expression expression, JavaType type) {
			this.expression = expression;
			this.type = type;
		}
		public Expression getExpression() {
			return expression;
		}
		public JavaType getType() {
			return type;
		}
        
    }

    private static class VariableAndTypeTree {
        private final J.VariableDeclarations.NamedVariable variable;
        private final TypeTree type;
		public VariableAndTypeTree(NamedVariable variable, TypeTree type) {
			this.variable = variable;
			this.type = type;
		}
		public J.VariableDeclarations.NamedVariable getVariable() {
			return variable;
		}
		public TypeTree getType() {
			return type;
		}
        
    }

    private static class InstanceOfPatternReplacements {
        private final J root;
        private final Map<ExpressionAndType, J.InstanceOf> instanceOfs = new HashMap<>();
        private final Map<J.InstanceOf, Set<J>> contexts = new HashMap<>();
        private final Map<J.InstanceOf, Set<Cursor>> contextScopes = new HashMap<>();
        private final Map<J.TypeCast, J.InstanceOf> replacements = new HashMap<>();
        private final Map<J.InstanceOf, VariableAndTypeTree> variablesToDelete = new HashMap<>();

        public InstanceOfPatternReplacements(J root) {
			this.root = root;
		}

		public void registerInstanceOf(J.InstanceOf instanceOf, Set<J> contexts) {
            Expression expression = instanceOf.getExpression();
            JavaType type = ((TypedTree) instanceOf.getClazz()).getType();
            if (type == null) {
                return;
            }

            Optional<ExpressionAndType> existing = instanceOfs.keySet().stream()
                    .filter(k -> TypeUtils.isAssignableTo(type, k.getType()) &&
                                 SemanticallyEqual.areEqual(k.getExpression(), expression))
                    .findAny();
            if (!existing.isPresent()) {
                instanceOfs.put(new ExpressionAndType(expression, type), instanceOf);
                this.contexts.put(instanceOf, contexts);
            }
        }

        public void registerTypeCast(J.TypeCast typeCast, Cursor cursor) {
            Expression expression = typeCast.getExpression();
            JavaType type = typeCast.getClazz().getTree().getType();

            Optional<ExpressionAndType> match = instanceOfs.keySet().stream()
                    .filter(k -> TypeUtils.isAssignableTo(type, k.getType()) &&
                                 SemanticallyEqual.areEqual(k.getExpression(), expression))
                    .findAny();
            if (match.isPresent()) {
                Cursor parent = cursor.getParentTreeCursor();
                J.InstanceOf instanceOf = instanceOfs.get(match.get());
                Set<J> validContexts = contexts.get(instanceOf);
                for (Iterator<?> it = cursor.getPath(); it.hasNext(); ) {
                    Object next = it.next();
                    if (validContexts.contains(next)) {
                        if (isAcceptableTypeCast(typeCast) && isTheSameAsOtherTypeCasts(typeCast, instanceOf)) {
                            if (parent.getValue() instanceof J.VariableDeclarations.NamedVariable &&
                                !variablesToDelete.containsKey(instanceOf)) {
                                variablesToDelete.put(instanceOf, new VariableAndTypeTree(parent.getValue(),
                                        requireNonNull(parent.firstEnclosing(J.VariableDeclarations.class).getTypeExpression())));
                            } else {
                                replacements.put(typeCast, instanceOf);
                            }
                            contextScopes.computeIfAbsent(instanceOf, k -> new HashSet<>()).add(cursor);
                        } else {
                            replacements.entrySet().removeIf(e -> e.getValue() == instanceOf);
                            variablesToDelete.remove(instanceOf);
                            contextScopes.remove(instanceOf);
                            contexts.remove(instanceOf);
                            instanceOfs.entrySet().removeIf(e -> e.getValue() == instanceOf);
                        }
                        break;
                    } else if (root == next) {
                        break;
                    }
                }
            }
        }

        private boolean isAcceptableTypeCast(J.TypeCast typeCast) {
            TypeTree typeTree = typeCast.getClazz().getTree();
            if (typeTree instanceof J.ParameterizedType) {
                return requireNonNull(((J.ParameterizedType) typeTree).getTypeParameters()).stream().allMatch(J.Wildcard.class::isInstance);
            }
            return true;
        }

        private boolean isTheSameAsOtherTypeCasts(J.TypeCast typeCast, J.InstanceOf instanceOf) {
            return replacements
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() == instanceOf)
                    .findFirst()
                    .map(e -> e.getKey().getType().equals(typeCast.getType()))
                    .orElse(true);
        }

        public boolean isEmpty() {
            return replacements.isEmpty() && variablesToDelete.isEmpty();
        }

        public J.InstanceOf processInstanceOf(J.InstanceOf instanceOf, Cursor cursor) {
            if (!contextScopes.containsKey(instanceOf)) {
                return instanceOf;
            }
            JavaType type = ((TypedTree) instanceOf.getClazz()).getType();
            String name = patternVariableName(instanceOf, cursor);
            J.InstanceOf result = instanceOf.withPattern(new J.Identifier(
                    randomId(),
                    Space.build(" ", emptyList()),
                    Markers.EMPTY,
                    emptyList(),
                    name,
                    type,
                    null));

            J currentTypeTree = instanceOf.getClazz();
            TypeTree typeCastTypeTree = computeTypeTreeFromTypeCasts(instanceOf);
            // If type tree from type cast is not parameterized then NVM. Instance of should already have proper type
            if (typeCastTypeTree instanceof J.ParameterizedType) {
                J.ParameterizedType parameterizedType = (J.ParameterizedType) typeCastTypeTree;
                result = result.withClazz(parameterizedType.withId(Tree.randomId()).withPrefix(currentTypeTree.getPrefix()));
            }

            // update entry in replacements to share the pattern variable name
            for (Map.Entry<J.TypeCast, J.InstanceOf> entry : replacements.entrySet()) {
                if (entry.getValue() == instanceOf) {
                    entry.setValue(result);
                }
            }
            return result;
        }

        private TypeTree computeTypeTreeFromTypeCasts(J.InstanceOf instanceOf) {
            TypeTree typeCastTypeTree = replacements
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() == instanceOf)
                    .findFirst()
                    .map(e -> e.getKey().getClazz().getTree())
                    .orElse(null);
            if (typeCastTypeTree == null) {
                VariableAndTypeTree variable = variablesToDelete.get(instanceOf);
                if (variable != null) {
                    typeCastTypeTree = variable.getType();
                }
            }
            return typeCastTypeTree;
        }

        private String patternVariableName(J.InstanceOf instanceOf, Cursor cursor) {
            VariableNameStrategy strategy;
            if (root instanceof J.If) {
                VariableAndTypeTree variableData = variablesToDelete.get(instanceOf);
                strategy = variableData != null ?
                        VariableNameStrategy.exact(variableData.getVariable().getSimpleName()) :
                        VariableNameStrategy.normal(contextScopes.get(instanceOf));
            } else {
                strategy = VariableNameStrategy.short_();
            }
            String baseName = strategy.variableName(((TypeTree) instanceOf.getClazz()).getType());
            return VariableNameUtils.generateVariableName(baseName, cursor, INCREMENT_NUMBER);
        }

        public @Nullable J processTypeCast(J.TypeCast typeCast, Cursor cursor) {
            J.InstanceOf instanceOf = replacements.get(typeCast);
            if (instanceOf != null && instanceOf.getPattern() != null) {
                String name = ((J.Identifier) instanceOf.getPattern()).getSimpleName();
                TypedTree owner = cursor.firstEnclosing(J.MethodDeclaration.class);
                owner = owner != null ? owner : cursor.firstEnclosingOrThrow(J.ClassDeclaration.class);
                JavaType.Variable fieldType = new JavaType.Variable(null, Flag.Default.getBitMask(), name, owner.getType(), typeCast.getType(), emptyList());
                return new J.Identifier(
                        randomId(),
                        typeCast.getPrefix(),
                        Markers.EMPTY,
                        emptyList(),
                        name,
                        typeCast.getType(),
                        fieldType);
            }
            return null;
        }

        public @Nullable J processVariableDeclarations(J.VariableDeclarations multiVariable) {
            return multiVariable.getVariables().stream().anyMatch(v -> variablesToDelete.values().stream().anyMatch(vd -> vd.getVariable() == v)) ? null : multiVariable;
        }
    }

    private static class UseInstanceOfPatternMatching extends JavaVisitor<Integer> {

        private final InstanceOfPatternReplacements replacements;

        public UseInstanceOfPatternMatching(InstanceOfPatternReplacements replacements) {
            this.replacements = replacements;
        }

        static @Nullable J refactor(@Nullable J tree, InstanceOfPatternReplacements replacements, Cursor cursor) {
            return new UseInstanceOfPatternMatching(replacements).visit(tree, 0, cursor);
        }

        @Override
        public J visitBinary(J.Binary original, Integer integer) {
            Expression newLeft = (Expression) super.visitNonNull(original.getLeft(), integer);
            if (newLeft != original.getLeft()) {
                // The left side changed, so the right side should see any introduced variable names
                J.Binary replacement = original.withLeft(newLeft);
                Cursor widenedCursor = updateCursor(replacement);

                Expression newRight;
                if (original.getRight() instanceof J.InstanceOf) {
                    newRight = replacements.processInstanceOf((J.InstanceOf) original.getRight(), widenedCursor);
                } else if (original.getRight() instanceof J.Parentheses &&
                           ((J.Parentheses<?>) original.getRight()).getTree() instanceof J.InstanceOf) {
                    @SuppressWarnings("unchecked")
                    J.Parentheses<J.InstanceOf> originalRight = (J.Parentheses<J.InstanceOf>) original.getRight();
                    newRight = originalRight.withTree(replacements.processInstanceOf(originalRight.getTree(), widenedCursor));
                } else {
                    newRight = (Expression) super.visitNonNull(original.getRight(), integer, widenedCursor);
                }
                return replacement.withRight(newRight);
            }
            // The left side didn't change, so the right side doesn't need to see any introduced variable names
            return super.visitBinary(original, integer);
        }

        @Override
        public J.InstanceOf visitInstanceOf(J.InstanceOf instanceOf, Integer executionContext) {
            instanceOf = (J.InstanceOf) super.visitInstanceOf(instanceOf, executionContext);
            instanceOf = replacements.processInstanceOf(instanceOf, getCursor());
            return instanceOf;
        }

        @Override
        public <T extends J> J visitParentheses(J.Parentheses<T> parens, Integer executionContext) {
            if (parens.getTree() instanceof J.TypeCast) {
                J replacement = replacements.processTypeCast((J.TypeCast) parens.getTree(), getCursor());
                if (replacement != null) {
                    return replacement.withPrefix(parens.getPrefix());
                }
            }
            return super.visitParentheses(parens, executionContext);
        }

        @Override
        public J visitTypeCast(J.TypeCast typeCast, Integer executionContext) {
            typeCast = (J.TypeCast) super.visitTypeCast(typeCast, executionContext);
            J replacement = replacements.processTypeCast(typeCast, getCursor());
            if (replacement != null) {
                return replacement;
            }
            return typeCast;
        }

        @Override
        public @Nullable J visitVariableDeclarations(J.VariableDeclarations multiVariable, Integer integer) {
            multiVariable = (J.VariableDeclarations) super.visitVariableDeclarations(multiVariable, integer);
            return replacements.processVariableDeclarations(multiVariable);
        }
    }

    private static class VariableNameStrategy {
        public static final Pattern NAME_SPLIT_PATTERN = Pattern.compile("[$._]*(?=\\p{Upper}+[\\p{Lower}\\p{Digit}]*)");
        private final Style style;

        @Nullable
        private final String name;

        private final Set<Cursor> contextScopes;

        enum Style {
            SHORT, NORMAL, EXACT
        }

        private VariableNameStrategy(Style style, @Nullable String exactName, Set<Cursor> contextScopes) {
            this.style = style;
            this.name = exactName;
            this.contextScopes = contextScopes;
        }

        static VariableNameStrategy short_() {
            return new VariableNameStrategy(Style.SHORT, null, Collections.emptySet());
        }

        static VariableNameStrategy normal(Set<Cursor> contextScopes) {
            return new VariableNameStrategy(Style.NORMAL, null, contextScopes);
        }

        static VariableNameStrategy exact(String name) {
            return new VariableNameStrategy(Style.EXACT, name, Collections.emptySet());
        }

        public String variableName(@Nullable JavaType type) {
            // the instanceof operator only accepts classes (without generics) and arrays
            if (style == Style.EXACT) {
                //noinspection DataFlowIssue
                return name;
            } else if (type instanceof JavaType.FullyQualified) {
                String className = ((JavaType.FullyQualified) type).getClassName();
                className = className.substring(className.lastIndexOf('.') + 1);
                String baseName = null;
                switch (style) {
                    case SHORT:
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < className.length(); i++) {
                            char c = className.charAt(i);
                            if (Character.isUpperCase(c)) {
                                builder.append(Character.toLowerCase(c));
                            }
                        }
                        baseName = builder.length() > 0 ? builder.toString() : "o";
                        break;
                    case NORMAL:
                        Set<String> namesInScope = contextScopes.stream()
                                .flatMap(c -> VariableNameUtils.findNamesInScope(c).stream())
                                .collect(Collectors.toSet());
                        List<String> nameSegments = Stream.of(NAME_SPLIT_PATTERN.split(className))
                                .filter(s -> !s.isEmpty()).collect(Collectors.toList());
                        for (int i = nameSegments.size() - 1; i >= 0; i--) {
                            String name = String.join("", nameSegments.subList(i, nameSegments.size()));
                            if (name.length() < 2) {
                                continue;
                            }
                            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                            if (!namesInScope.contains(name)) {
                                baseName = name;
                                break;
                            }
                        }
                        if (baseName == null) {
                            baseName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
                        }
                        break;
                    default:
                        baseName = "obj";
                }
                String candidate = baseName;
                OUTER:
                while (true) {
                    for (Cursor scope : contextScopes) {
                        String newCandidate = VariableNameUtils.generateVariableName(candidate, scope, INCREMENT_NUMBER);
                        if (!newCandidate.equals(candidate)) {
                            candidate = newCandidate;
                            continue OUTER;
                        }
                    }
                    break;
                }
                return candidate;
            } else if (type instanceof JavaType.Primitive) {
                String keyword = ((JavaType.Primitive) type).getKeyword();
                return style == Style.SHORT ? keyword.substring(0, 1) : keyword;
            } else if (type instanceof JavaType.Array) {
                JavaType elemType = ((JavaType.Array) type).getElemType();
                while (elemType instanceof JavaType.Array) {
                    elemType = ((JavaType.Array) elemType).getElemType();
                }
                return variableName(elemType) + 's';
            }
            return style == Style.SHORT ? "o" : "obj";
        }
    }
}
