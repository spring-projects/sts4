package org.springframework.ide.vscode.boot.java.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.openrewrite.Cursor;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.Range;

public class ORAstUtils {
	
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

}
