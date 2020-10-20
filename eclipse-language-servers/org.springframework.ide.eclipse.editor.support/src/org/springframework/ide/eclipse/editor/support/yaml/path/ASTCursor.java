package org.springframework.ide.eclipse.editor.support.yaml.path;

import java.util.stream.Stream;

import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.Node;

/**
 * Pointer to a specific node in Snake yaml parse tree. Supports navigation
 * using {@link YamlPath}s, including support for 'ambiguous' steps like
 * {@link YamlPathSegment}.anyChild()
 * <p>
 * Because the 'root node' of a parsed Yaml file is not actually {@link Node}
 * in snake yaml. A cursor pointing at the root of a tree is implemented
 * differently than a pointer to a node inside the tree. Therefore
 * this class is abstract and has two concrete subclasses.
 *
 * @author Kris De Volder
 */
public abstract class ASTCursor implements YamlNavigable<ASTCursor> {

	public abstract Stream<ASTCursor> traverseAmbiguously(YamlPathSegment s);

	/**
	 * Return the node this cursor is pointing to. This is either a {@link Node} or
	 * a {@link YamlFileAST}.
	 */
	public abstract Object getNode();

}
