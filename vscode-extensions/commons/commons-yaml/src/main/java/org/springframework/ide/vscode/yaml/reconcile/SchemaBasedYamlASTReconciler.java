package org.springframework.ide.vscode.yaml.reconcile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.commons.reconcile.IProblemCollector;
import org.springframework.ide.vscode.util.ExceptionUtil;
import org.springframework.ide.vscode.util.StringUtil;
import org.springframework.ide.vscode.yaml.ast.NodeUtil;
import org.springframework.ide.vscode.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.yaml.schema.YType;
import org.springframework.ide.vscode.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.yaml.schema.YTypedProperty;
import org.springframework.ide.vscode.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.yaml.util.ValueParser;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class SchemaBasedYamlASTReconciler implements YamlASTReconciler {

	private final IProblemCollector problems;
	private final YamlSchema schema;
	private final YTypeUtil typeUtil;

	public SchemaBasedYamlASTReconciler(IProblemCollector problems, YamlSchema schema) {
		this.problems = problems;
		this.schema = schema;
		this.typeUtil = schema.getTypeUtil();
	}

	@Override
	public void reconcile(YamlFileAST ast) {
		List<Node> nodes = ast.getNodes();
		if (nodes!=null && !nodes.isEmpty()) {
			for (Node node : nodes) {
				reconcile(node, schema.getTopLevelType());
			}
		}
	}

	private void reconcile(Node node, YType type) {
		if (type!=null) {
			switch (node.getNodeId()) {
			case mapping:
				MappingNode map = (MappingNode) node;
				if (typeUtil.isMap(type)) {
					for (NodeTuple entry : map.getValue()) {
						reconcile(entry.getKeyNode(), typeUtil.getKeyType(type));
						reconcile(entry.getValueNode(), typeUtil.getDomainType(type));
					}
				} else if (typeUtil.isBean(type)) {
					Map<String, YTypedProperty> beanProperties = typeUtil.getPropertiesMap(type);
					for (NodeTuple entry : map.getValue()) {
						Node keyNode = entry.getKeyNode();
						String key = NodeUtil.asScalar(keyNode);
						if (key==null) {
							expectScalar(node);
						} else {
							YTypedProperty prop = beanProperties.get(key);
							if (prop==null) {
								unknownBeanProperty(keyNode, type, key);
							} else {
								reconcile(entry.getValueNode(), prop.getType());
							}
						}
					}
				} else {
					expectTypeButFoundMap(type, node);
				}
				break;
			case sequence:
				SequenceNode seq = (SequenceNode) node;
				if (typeUtil.isSequencable(type)) {
					for (Node el : seq.getValue()) {
						reconcile(el, typeUtil.getDomainType(type));
					}
				} else {
					expectTypeButFoundSequence(type, node);
				}
				break;
			case scalar:
				if (typeUtil.isAtomic(type)) {
					ValueParser parser = typeUtil.getValueParser(type);
					if (parser!=null) {
						try {
							parser.parse(NodeUtil.asScalar(node));
						} catch (Exception e) {
							String msg = ExceptionUtil.getMessage(e);
							valueParseError(type, node, msg);
						}
					}
				} else {
					expectTypeButFoundScalar(type, node);
				}
				break;
			default:
				// other stuff we don't check
			}
		}
	}

	private void valueParseError(YType type, Node node, String parseErrorMsg) {
		String msg= "Couldn't parse as '"+describe(type)+"'";
		if (StringUtil.hasText(parseErrorMsg)) {
			msg += " ("+parseErrorMsg+")";
		}
		problem(node, msg);
	}

	private void unknownBeanProperty(Node keyNode, YType type, String name) {
		problem(keyNode, "Unknown property '"+name+"' for type '"+typeUtil.niceTypeName(type)+"'");
	}

	private void expectScalar(Node node) {
		problem(node, "Expecting a 'Scalar' node but got "+describe(node));
	}

	private String describe(Node node) {
		switch (node.getNodeId()) {
		case scalar:
			return "'"+((ScalarNode)node).getValue()+"'";
		case mapping:
			return "a 'Mapping' node";
		case sequence:
			return "a 'Sequence' node";
		case anchor:
			return "a 'Anchor' node";
		default:
			throw new IllegalStateException("Missing switch case");
		}
	}

	private void expectTypeButFoundScalar(YType type, Node node) {
		problem(node, "Expecting a '"+describe(type)+"' but found a 'Scalar'");
	}

	private void expectTypeButFoundSequence(YType type, Node node) {
		problem(node, "Expecting a '"+describe(type)+"' but found a 'Sequence'");
	}

	private void expectTypeButFoundMap(YType type, Node node) {
		problem(node, "Expecting a '"+describe(type)+"' but found a 'Map'");
	}

	private String describe(YType type) {
		if (typeUtil.isAtomic(type)) {
			return typeUtil.niceTypeName(type);
		}
		ArrayList<String> expectedNodeTypes = new ArrayList<>();
		if (typeUtil.isBean(type) || typeUtil.isMap(type)) {
			expectedNodeTypes.add("Map");
		}
		if (typeUtil.isSequencable(type)) {
			expectedNodeTypes.add("Sequence");
		}
		return StringUtil.collectionToDelimitedString(expectedNodeTypes, " or ");
	}

	private void problem(Node node, String msg) {
		problems.accept(YamlSchemaProblems.schemaProblem(msg, node));
	}

}
