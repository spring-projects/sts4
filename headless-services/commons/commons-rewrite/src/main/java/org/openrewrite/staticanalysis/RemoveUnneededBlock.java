//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.openrewrite.staticanalysis;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Incubating;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

@Incubating(
    since = "7.21.0"
)
public class RemoveUnneededBlock extends Recipe {
    public RemoveUnneededBlock() {
    }

    public String getDisplayName() {
        return "Remove unneeded block";
    }

    public String getDescription() {
        return "Flatten blocks into inline statements when possible.";
    }

    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RemoveUnneededBlockStatementVisitor();
    }

    static class RemoveUnneededBlockStatementVisitor extends JavaVisitor<ExecutionContext> {
        RemoveUnneededBlockStatementVisitor() {
        }

        public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
            J.Block bl = (J.Block)super.visitBlock(block, ctx);
            J directParent = (J)this.getCursor().getParentTreeCursor().getValue();
            return !(directParent instanceof J.NewClass) && !(directParent instanceof J.ClassDeclaration) ? this.maybeInlineBlock(bl, ctx) : bl;
        }

        private J.Block maybeInlineBlock(J.Block block, ExecutionContext ctx) {
            List<Statement> statements = block.getStatements();
            if (statements.isEmpty()) {
                return block;
            } else {
                Statement lastStatement = (Statement)statements.get(statements.size() - 1);
                J.Block flattened = block.withStatements(ListUtils.flatMap(statements, (i, stmt) -> {
                    J.Block nested;
                    if (stmt instanceof J.Try) {
                        J.Try _try = (J.Try)stmt;
                        if (_try.getResources() != null || !_try.getCatches().isEmpty() || _try.getFinally() == null || !_try.getFinally().getStatements().isEmpty()) {
                            return stmt;
                        }

                        nested = _try.getBody();
                    } else {
                        if (!(stmt instanceof J.Block)) {
                            return stmt;
                        }

                        nested = (J.Block)stmt;
                    }

                    if (i < statements.size() - 1) {
                        Stream var10000 = nested.getStatements().stream();
                        Objects.requireNonNull(J.VariableDeclarations.class);
                        if (var10000.anyMatch(J.VariableDeclarations.class::isInstance)) {
                            return stmt;
                        }
                    }

                    return ListUtils.map(nested.getStatements(), (j, inlinedStmt) -> {
                        if (j == 0) {
                            inlinedStmt = (Statement)inlinedStmt.withPrefix(inlinedStmt.getPrefix().withComments(ListUtils.concatAll(nested.getComments(), inlinedStmt.getComments())));
                        }

                        return (Statement)this.autoFormat(inlinedStmt, ctx, this.getCursor());
                    });
                }));
                if (flattened == block) {
                    return block;
                } else {
                    if (lastStatement instanceof J.Block) {
                        flattened = flattened.withEnd(flattened.getEnd().withComments(ListUtils.concatAll(((J.Block)lastStatement).getEnd().getComments(), flattened.getEnd().getComments())));
                    }

                    return flattened;
                }
            }
        }
    }
}
