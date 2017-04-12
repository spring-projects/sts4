/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.yaml.quickfix;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

public class ReplaceDeprecatedYamlQuickfix implements ICompletionProposal {

//	public static ProblemFixer FIXER = (context, problem, proposals) -> {
//		throw new UnsupportedOperationException("Not yet implemented");
//		PropertyInfo metadata = problem.getMetadata();
//		if (metadata!=null) {
//			String replacement = metadata.getDeprecationReplacement();
//			if (replacement!=null) {
//				//No need to check problem type...  we only attach this fixer to problems of applicable type.
//				proposals.add(new ReplaceDeprecatedYamlQuickfix(context, problem));
//			}
//		}
//	};

	@Override
	public ICompletionProposal deemphasize() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public String getLabel() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public CompletionItemKind getKind() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public DocumentEdits getTextEdit() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public String getDetail() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	@Override
	public Renderable getDocumentation() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

//	private final QuickfixContext context;
//	private final SpringPropertyProblem problem;
//
//	private LazyProposalApplier applier = new LazyProposalApplier() {
//		protected ProposalApplier create() throws Exception {
//			String newName = problem.getMetadata().getDeprecationReplacement();
//			String oldName = problem.getPropertyName();
//			YamlPath newPath = YamlPath.fromProperty(newName);
//			YamlPath oldPath = YamlPath.fromProperty(oldName);
//			YamlPath prefix = newPath.commonPrefix(oldPath);
//			if (prefix.size()==newPath.size()-1 && newPath.size()==oldPath.size()) {
//				//only the last segment has changed. We can do a simple 'in-place' replace
//				// of just the change segment.
//				DocumentEdits edits = new DocumentEdits(context.getDocument());
//				edits.replace(problem.getOffset(), problem.getEnd(), newPath.getLastSegment().toPropString());
//				return edits;
//			}
//			YamlDocument doc = new YamlDocument(context.getDocument(), YamlStructureProvider.DEFAULT);
//			SNode problemNode = doc.getStructure().find(problem.getOffset());
//			if (problemNode.getNodeType()==SNodeType.KEY) {
//				SKeyNode problemKey = (SKeyNode) problemNode;
//				if (problemKey.isInKey(problem.getOffset())) {
//					YamlPathEdits edits = new YamlPathEdits(doc);
////					print(doc, edits);
//					String valueText = problemKey.getValueWithRelativeIndent();
//					edits.deleteNode(problemKey);
//					int maxParentDeletions = oldPath.size() - prefix.size() - 1; // don't delete bits of the common prefix!
//					SChildBearingNode parent = problemNode.getParent();
//					while (maxParentDeletions>0 && parent!=null && parent.getChildren().size()==1) {
//						edits.deleteNode(parent);
//						parent = parent.getParent();
//						maxParentDeletions--;
//					}
////					print(doc, edits);
//					SDocNode docRoot = problemNode.getDocNode(); //edits should stay within the same 'document' for yaml file that has multiple documents inside of it.
//					edits.createPath(docRoot, YamlPath.fromProperty(newName), valueText);
////					print(doc, edits);
//					return edits;
//				}
//			}
//			//Not sure what to do... case not covered... so do nothing but tell the user.
//			context.getUI().error("Yaml file too complex",
//					"Sorry, but the yaml file is too complex for this quickfix. " +
//					"Please make the change manually."
//			);
//			return ProposalApplier.NULL;
//		}
//
////		private void print(YamlDocument doc, YamlPathEdits edits) throws Exception {
////			Document workingCopy = new Document(doc.getDocument().get());
////			edits.apply(workingCopy);
////			System.out.println("==============");
////			System.out.println(workingCopy.get());
////			System.out.println("==============");
////		}
//	};
//
//	public ReplaceDeprecatedYamlQuickfix(QuickfixContext context, SpringPropertyProblem problem) {
//		this.context = context;
//		this.problem = problem;
//	}
//
//	@Override
//	public void apply(IDocument doc) {
//		try {
//			applier.apply(doc);
//		} catch (Exception e) {
//			Log.log(e);
//		}
//	}
//
//	private String getReplacementProperty() {
//		return problem.getMetadata().getDeprecationReplacement();
//	}
//
//	@Override
//	public Point getSelection(IDocument doc) {
//		try {
//			return applier.getSelection(doc);
//		} catch (Exception e) {
//			Log.log(e);
//			return null;
//		}
//	}
//
//	@Override
//	public String getAdditionalProposalInfo() {
//		return null;
//	}
//
//	@Override
//	public String getDisplayString() {
//		return "Change to '"+getReplacementProperty()+"'";
//	}
//
//	@Override
//	public Image getImage() {
//		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
//	}
//
//	@Override
//	public IContextInformation getContextInformation() {
//		return null;
//	}

}
