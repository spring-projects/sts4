package org.springframework.ide.vscode.boot.java.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.openrewrite.Cursor;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.Assignment;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.J.EnumValueSet;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.NewArray;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class ORAstUtils {
	
	private static final Logger log = LoggerFactory.getLogger(ORAstUtils.class);
	
	private static class AncestersMarker implements Marker {
		
		private UUID uuid;
		private List<J> ancesters = List.of();
		
		public AncestersMarker(List<J> ancesters) {
			this.uuid = Tree.randomId();
			this.ancesters = ancesters;
		}

		@Override
		public UUID getId() {
			return uuid;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getFirstAnsector(Class<T> clazz) {
			if (ancesters != null) {
				for (J node : ancesters) {
					if (clazz.isInstance(node)) {
						return (T) node;
					}
				}
			}
			return null;
		}
		
		public J getParent() {
			if (ancesters != null && !ancesters.isEmpty()) {
				return ancesters.get(0);
			}
			return null;
		}
	}
	
	public static J findAstNodeAt(CompilationUnit cu, int offset) {
		AtomicReference<J> f = new AtomicReference<>();
		new JavaIsoVisitor<AtomicReference<J>>() {
			public J visit(Tree tree, AtomicReference<J> found) {
				if (tree == null) {
					return null;
				}
				if (found.get() == null && tree instanceof J) {
					J node = (J) tree;
					Range range = node.getMarkers().findFirst(Range.class).orElse(null);
					if (range != null
							&& range.getStart().getOffset() <= offset
							&& offset <= range.getEnd().getOffset()) {
						super.visit(tree, found);
						if (found.get() == null) {
							List<J> ancesters = new ArrayList<>();
							for (Cursor c = getCursor(); c != null && !(c.getValue() instanceof SourceFile); c = c.getParent()) {
								Object o = c.getValue();
								if (o instanceof J) {
									ancesters.add((J) o);
								}
							}
							J n = node.withMarkers(node.getMarkers().addIfAbsent(new AncestersMarker(ancesters)));
							found.set(n);
							return n;
						}
					} else {
						return (J) tree;
					}
				}
				return (J) tree;
			};
		}.visitNonNull(cu, f);
		return f.get();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T findNode(J node, Class<T> clazz) {
		if (clazz.isInstance(node)) {
			return (T) node;
		}
		AncestersMarker ancestry = node.getMarkers().findFirst(AncestersMarker.class).orElseThrow();
		return ancestry.getFirstAnsector(clazz);
	}
	
	public static J getParent(J node) {
		AncestersMarker ancestry = node.getMarkers().findFirst(AncestersMarker.class).orElseThrow();
		return ancestry.getParent();
	}
	
    public static EnumValueSet getEnumValues(ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.EnumValueSet.class::isInstance)
                .map(J.EnumValueSet.class::cast)
                .findAny()
                .orElse(null);
    }

    public static List<VariableDeclarations> getFields(ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .collect(Collectors.toList());
    }

    public static List<MethodDeclaration> getMethods(ClassDeclaration classDecl) {
        return classDecl.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

	public static DocumentRegion nodeRegion(TextDocument doc, J node) {
		Range r = node.getMarkers().findFirst(Range.class).orElseThrow();
		int start = r.getStart().getOffset();
		int end = r.getEnd().getOffset();
		return new DocumentRegion(doc, start, end);
	}
	
	public static DocumentRegion nameRegion(TextDocument doc, Annotation annotation) {
		Range r = annotation.getAnnotationType().getMarkers().findFirst(Range.class).orElseThrow();
		int start = r.getStart().getOffset();
		int end = r.getEnd().getOffset();
		// TODO: OR AST what about '@'???
		if (doc.getSafeChar(start - 1) == '@') {
			start--;
		}
		return new DocumentRegion(doc, start, end);
	}

	public static Optional<org.eclipse.lsp4j.Range> nameRange(TextDocument doc, Annotation annotation) {
		try {
			return Optional.of(nameRegion(doc, annotation).asRange());
		} catch (Exception e) {
			log.error("", e);
			return Optional.empty();
		}
	}

	public static MethodDeclaration getAnnotatedMethod(Annotation annotation) {
		J parent = getParent(annotation);
		if (parent instanceof MethodDeclaration) {
			return (MethodDeclaration)parent;
		}
		return null;
	}

	public static Annotation getBeanAnnotation(MethodDeclaration method) {
		for (Annotation a : method.getLeadingAnnotations()) {
			FullyQualified type = TypeUtils.asFullyQualified(a.getType());
			if (type != null) {
				if (Annotations.BEAN.equals(type.getFullyQualifiedName())) {
					return a;
				}
			}
		}
		return null;
	}

	public static Optional<Expression> getAttribute(Annotation annotation, String name) {
		if (annotation != null) {
			try {
				List<Expression> args = annotation.getArguments();
				if (name.equals("value") && args.size() == 1 && !(args.get(0) instanceof Assignment)) {
					return Optional.ofNullable(args.get(0));
				} else {
					for (Expression arg : args) {
						if (arg instanceof Assignment) {
							Assignment assignment  = (Assignment) arg;
							if (name.equals(assignment.getVariable().printTrimmed())) {
								return Optional.ofNullable(assignment.getAssignment());
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return Optional.empty();
	}

	/**
	 * For case where a expression can be either a String or a array of Strings and
	 * we are interested in the first element of the array. (I.e. typical case
	 * when annotation attribute is of type String[] (because Java allows using a single
	 * value as a convenient syntax for writing an array of length 1 in that case.
	 */
	public static Optional<String> getFirstString(Expression exp) {
		if (exp instanceof Literal) {
			return Optional.ofNullable(getLiteralValue((Literal) exp));
		} else if (exp instanceof NewArray) {
			NewArray array = (NewArray) exp;
			List<Expression> entries = array.getInitializer();
			if (entries != null) {
				for (Expression e : entries) {
					Optional<String> s = getFirstString(e);
					if (s.isPresent()) {
						return s;
					}
				}
			}
		}
		return Optional.empty();
	}

	public static String getLiteralValue(Literal node) {
		return node.getValueSource();
	}

	public static ClassDeclaration findDeclaringType(J node) {
		return node == null ? null : findNode(node, ClassDeclaration.class);
	}
	
	public static Range getRange(J node) {
		return node.getMarkers().findFirst(Range.class).orElseThrow();
	}

	public static Optional<String> beanId(List<Annotation> annotations) {
		return annotations.stream()
				.filter(a -> {
					FullyQualified type = TypeUtils.asFullyQualified(a.getType());
					if (type != null) {
						return Annotations.QUALIFIER.equals(type.getFullyQualifiedName());
					}
					return false;
				})
				.findFirst()
				.map(a -> a.getArguments())
				.filter(args -> args != null && !args.isEmpty())
				.map(args -> args.get(0))
				.filter(Literal.class::isInstance)
				.map(arg -> arg.printTrimmed());
	}

	public static Collection<Annotation> getAnnotations(ClassDeclaration declaringType) {
		return declaringType.getLeadingAnnotations();
	}

	public static boolean hasExactlyOneConstructor(ClassDeclaration typeDecl) {
		boolean oneFound = false;
		for (MethodDeclaration methodDeclaration : getMethods(typeDecl)) {
			if (methodDeclaration.isConstructor()) {
				if (oneFound) {
					return false;
				} else {
					oneFound = true;
				}
			}
		}
		return oneFound;
	}

	public static ClassDeclaration getAnnotatedType(Annotation annotation) {
		J parent = getParent(annotation);
		if (parent instanceof ClassDeclaration) {
			return (ClassDeclaration)parent;
		}
		return null;
	}


}
