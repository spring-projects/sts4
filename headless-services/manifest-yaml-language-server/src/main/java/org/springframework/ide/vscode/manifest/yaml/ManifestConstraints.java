package org.springframework.ide.vscode.manifest.yaml;

import java.util.Arrays;
import java.util.Set;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.constraints.Constraint;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;

public class ManifestConstraints {

	public static Constraint mutuallyExclusive(String... propertyIds) {
		return (dc, parent, node, type, problems) -> {
			Set<String> keys = NodeUtil.getScalarKeys(parent);
			Arrays.stream(propertyIds).filter(id -> keys.contains(id)).findFirst().ifPresent(propertyId -> {
				// Find key node, because the node parameter is the value node
				MappingNode mapNode = (MappingNode) parent;
				mapNode.getValue().stream().filter(t -> t.getValueNode() == node).findFirst().ifPresent(t -> {
					Node keyNode = t.getKeyNode();
					int start = keyNode.getStartMark().getIndex();
					int end = keyNode.getEndMark().getIndex();
					problems.accept(
							new ReconcileProblemImpl(ManifestYamlSchemaProblemsTypes.MUTUALLY_EXCLUSIVE_PROPERTY_PROBLEM,
									"Property cannot co-exist with property '" + propertyId + "'", start, end - start));
				});;
			});
		};
	}

}
