package org.springframework.ide.vscode.commons.rewrite.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;

import com.google.common.collect.ImmutableList;

public class AnnotationHierarchies {

	public static Collection<FullyQualified> getDirectSuperAnnotations(FullyQualified type, Predicate<FullyQualified> ignore) {
		List<FullyQualified> annotations = type.getAnnotations();
		if (annotations != null && !annotations.isEmpty()) {
			ImmutableList.Builder<FullyQualified> superAnnotations = ImmutableList.builder();
			for (FullyQualified ab : annotations) {
				if (ignore == null || !ignore.test(ab)) {
					superAnnotations.add(ab);
				}
			}
			return superAnnotations.build();
		}

		return ImmutableList.of();
	}

	public static Set<String> getTransitiveSuperAnnotations(FullyQualified type, Predicate<FullyQualified> ignore) {
		Set<String> seen = new HashSet<>();
		if (type != null) {
			findTransitiveSupers(type, seen, ignore).collect(Collectors.toList());
		}
		return seen;
	}

	public static Stream<FullyQualified> findTransitiveSupers(FullyQualified type, Set<String> seen, Predicate<FullyQualified> ignore) {
		String qname = type.getFullyQualifiedName();
		if (seen.add(qname)) {
			return Stream.concat(Stream.of(type), getDirectSuperAnnotations(type, ignore).stream()
					.flatMap(superAnnotation -> findTransitiveSupers(superAnnotation, seen, ignore)));
		}
		return Stream.empty();
	}

	public static boolean isSubtypeOf(Annotation annotation, String fqAnnotationTypeName) {
		FullyQualified annotationType = TypeUtils.asFullyQualified(annotation.getType());
		if (annotationType != null) {
			return findTransitiveSupers(annotationType, new HashSet<>(), null)
					.anyMatch(superType -> fqAnnotationTypeName.equals(superType.getFullyQualifiedName()));
		}
		return false;
	}

}