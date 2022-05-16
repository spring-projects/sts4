/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORAstUtils {
		
	private static final Logger log = LoggerFactory.getLogger(ORAstUtils.class);
	
//	private static class ParentMarker implements Marker {
//		
//		private UUID uuid;
//		private J parent;
//		
//		public ParentMarker(J parent) {
//			this.uuid = Tree.randomId();
//			this.parent = parent;
//		}
//		
//		@Override
//		public UUID getId() {
//			return uuid;
//		}
//		
//		public J getParent() {
//			return parent;
//		}
//		
//		public J getGrandParent() {
//			if (parent != null) {
//				return parent.getMarkers().findFirst(ParentMarker.class).map(m -> m.getParent()).orElse(null);
//			}
//			return null;
//		}
//		
//		public <T> T getFirstAnsector(Class<T> clazz) {
//			if (clazz.isInstance(parent)) {
//				return clazz.cast(parent);
//			} else if (parent != null) {
//				return parent.getMarkers().findFirst(ParentMarker.class).map(m -> m.getFirstAnsector(clazz)).orElse(null);
//			}
//			return null;
//		}
//
//		@Override
//		public <T extends Tree> T withId(UUID id) {
//			this.uuid = id;
//			return (T) this;
//		}
//	}
//	
//	private static class AncestersMarker implements Marker {
//		
//		private UUID uuid;
//		private List<J> ancesters = List.of();
//		
//		public AncestersMarker(List<J> ancesters) {
//			this.uuid = Tree.randomId();
//			this.ancesters = ancesters;
//		}
//
//		@Override
//		public UUID getId() {
//			return uuid;
//		}
//		
//		@SuppressWarnings("unchecked")
//		public <T> T getFirstAnsector(Class<T> clazz) {
//			if (ancesters != null) {
//				for (J node : ancesters) {
//					if (clazz.isInstance(node)) {
//						return (T) node;
//					}
//				}
//			}
//			return null;
//		}
//		
//		public J getParent() {
//			if (ancesters != null && !ancesters.isEmpty()) {
//				return ancesters.get(0);
//			}
//			return null;
//		}
//
//		public J getGrandParent() {
//			if (ancesters != null && ancesters.size() > 1) {
//				return ancesters.get(1);
//			}
//			return null;
//		}
//	}
//	
//	private static class MarkParentRecipe extends Recipe {
//
//		@Override
//		public String getDisplayName() {
//			return "Create parent AST node references via markers";
//		}
//		
//		@Override
//		protected TreeVisitor<?, ExecutionContext> getVisitor() {
//			return new JavaIsoVisitor<>() {
//				
//				private Cursor parentCursor(Class<?> clazz) {
//					for (Cursor c = getCursor(); c != null
//							&& !(c.getValue() instanceof SourceFile); c = c.getParent()) {
//						Object o = c.getValue();
//						if (clazz.isInstance(o)) {
//							return c;
//						}
//					}
//					return null;
//				}
//				
//				@Override
//				public J visit(Tree tree, ExecutionContext p) {
//					if (tree instanceof J) {
//						J j = (J) tree;
//						J newJ = super.visit(j, p).withMarkers(j.getMarkers().addIfAbsent(new ParentMarker(null)));
//						
//						List<J> children = p.pollMessage(j.getId().toString(), Collections.emptyList());
//						for (J child : children) {
//							child.getMarkers().findFirst(ParentMarker.class).map(m -> m.parent = newJ);
//						}
//						
//						// Prepare myself for the parent;
//
//						Cursor parentCursor = parentCursor(J.class);
//						if (parentCursor != null) {
//							J parent = parentCursor.getValue();
//							String parentId = parent.getId().toString();
//							List<J> siblings = p.pollMessage(parentId, new ArrayList<J>());
//							siblings.add(newJ);
//							p.putMessage(parentId, siblings);
//						}
//						return newJ;
//					}
//					return (J) tree; 
//				}
//			};
//		}
//		
//	}
//	
//	public static J findAstNodeAt(CompilationUnit cu, int offset) {
//		AtomicReference<J> f = new AtomicReference<>();
//		new JavaIsoVisitor<AtomicReference<J>>() {
//			public J visit(Tree tree, AtomicReference<J> found) {
//				if (tree == null) {
//					return null;
//				}
//				if (found.get() == null && tree instanceof J) {
//					J node = (J) tree;
//					Range range = node.getMarkers().findFirst(Range.class).orElse(null);
//					if (range != null
//							&& range.getStart().getOffset() <= offset
//							&& offset <= range.getEnd().getOffset()) {
//						super.visit(tree, found);
//						if (found.get() == null) {
//							found.set(node);
//							return node;
//						}
//					} else {
//						return (J) tree;
//					}
//				}
//				return (J) tree;
//			};
//		}.visitNonNull(cu, f);
//		return f.get();
//	}
//	
//	@SuppressWarnings("unchecked")
//	public static <T> T findNode(J node, Class<T> clazz) {
//		if (clazz.isInstance(node)) {
//			return (T) node;
//		}
//		return node.getMarkers().findFirst(ParentMarker.class).map(m -> m.getFirstAnsector(clazz)).orElse(null); 
//	}
//	
//	public static J getParent(J node) {
//		return node.getMarkers().findFirst(ParentMarker.class).map(m -> m.getParent()).orElse(null);
//	}
	
	public static List<CompilationUnit> parse(JavaParser parser, Iterable<Path> sourceFiles) {
		InMemoryExecutionContext ctx = new InMemoryExecutionContext(e -> log.error("", e));
		ctx.putMessage(JavaParser.SKIP_SOURCE_SET_TYPE_GENERATION, true);
		List<CompilationUnit> cus = parser.parse(sourceFiles, null, ctx);
		return cus;
//		List<Result> results = new UpdateSourcePositions().doNext(new MarkParentRecipe()).run(cus);
//		return results.stream().map(r -> r.getAfter() == null ? r.getBefore() : r.getAfter()).map(CompilationUnit.class::cast).collect(Collectors.toList());
	}
	
	public static List<CompilationUnit> parseInputs(JavaParser parser, Iterable<Parser.Input> inputs) {
		InMemoryExecutionContext ctx = new InMemoryExecutionContext(e -> log.error("", e));
		ctx.putMessage(JavaParser.SKIP_SOURCE_SET_TYPE_GENERATION, true);
		List<CompilationUnit> cus = parser.parseInputs(inputs, null, ctx);
		return cus;
//		List<Result> results = new UpdateSourcePositions().doNext(new MarkParentRecipe()).run(cus);
//		return results.stream().map(r -> r.getAfter() == null ? r.getBefore() : r.getAfter()).map(CompilationUnit.class::cast).collect(Collectors.toList());
	}

    public static J.EnumValueSet getEnumValues(J.ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.EnumValueSet.class::isInstance)
                .map(J.EnumValueSet.class::cast)
                .findAny()
                .orElse(null);
    }

    public static List<J.VariableDeclarations> getFields(J.ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .collect(Collectors.toList());
    }

    public static List<J.MethodDeclaration> getMethods(J.ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public static String getSimpleName(String fqName) {
        int idx = fqName.lastIndexOf('.');
        if (idx < fqName.length() - 1) {
            return fqName.substring(idx + 1);
        }
        return fqName;
    }
    
    @SuppressWarnings("unchecked")
	private static TreeVisitor<?, ExecutionContext> getVisitor(Recipe r) {
    	try {
	    	Method m = Recipe.class.getDeclaredMethod("getVisitor");
	    	m.setAccessible(true);
	    	return (TreeVisitor<?, ExecutionContext>) m.invoke(r);
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
	private static List<TreeVisitor<J, ExecutionContext>> getAfterVisitors(TreeVisitor<J, ExecutionContext> visitor) {
    	try {
	    	Method m = TreeVisitor.class.getDeclaredMethod("getAfterVisit");
	    	m.setAccessible(true);
	    	return (List<TreeVisitor<J, ExecutionContext>>) m.invoke(visitor);
    	} catch (Exception e) {
    		return Collections.emptyList();
    	}
    }
    
	private static void makeVisitorNonTopLevel(JavaVisitor<ExecutionContext> visitor) {
    	try {
	    	Field f = TreeVisitor.class.getDeclaredField("afterVisit");
	    	f.setAccessible(true);
	    	f.set(visitor, new ArrayList<>());
    	} catch (Exception e) {
    		// ignore
    	}
	}
	
	public static Recipe nodeRecipe(JavaVisitor<ExecutionContext> v, Predicate<J> condition) {
    	return new NodeRecipe((JavaVisitor<ExecutionContext>) v, condition);
    }
	
    @SuppressWarnings("unchecked")
	public static Recipe nodeRecipe(Recipe r, Predicate<J> condition) {
    	return new NodeRecipe((JavaVisitor<ExecutionContext>) getVisitor(r), condition);
    }
    
    private static class NodeRecipe extends Recipe {
    	
    	private JavaVisitor<ExecutionContext> visitor;
    	private Predicate<J> condition;
    	
    	public NodeRecipe(JavaVisitor<ExecutionContext> visitor, Predicate<J> condition) {
    		this.visitor = visitor;
    		this.condition = condition;
    	}

    	@Override
    	public String getDisplayName() {
    		return "";
    	}

    	@Override
    	protected TreeVisitor<?, ExecutionContext> getVisitor() {
    		return new JavaVisitor<>() {
    			
    			@Override
    			public J visit(Tree tree, ExecutionContext ctx) {
    				J t = super.visit(tree, ctx);
    				if (condition.test(t)) {
    					makeVisitorNonTopLevel(visitor);
    					t = visitor.visit(t, ctx, getCursor());
    					for (TreeVisitor<J, ExecutionContext> v : getAfterVisitors(visitor)) {
    						doAfterVisit(v);
    					}
    				}
    				return t;
    			}

    		};
    	}	
    }


}
