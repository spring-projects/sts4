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
package org.springframework.ide.vscode.commons.rewrite.maven;

import java.util.Objects;
import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

public class ChangeDependencyClassifier extends Recipe {

    @Option(displayName = "Group",
            description = "The first part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "com.google.guava")
    String groupId;

    @Option(displayName = "Artifact",
            description = "The second part of a dependency coordinate 'com.google.guava:guava:VERSION'.",
            example = "guava")
    String artifactId;

    /**
     * If null, strips the scope from an existing dependency.
     */
    @Option(displayName = "New classifier",
            description = "Classifier to apply to specified Maven dependency. " +
                    "May be omitted, which indicates that no classifier should be added and any existing scope be removed from the dependency.",
            example = "jar",
            required = false)
    @Nullable
    String newClassifier;
    
	@Override
    public String getDisplayName() {
        return "Change Maven dependency classifier";
    }

    @Override
    public String getDescription() {
        return "Add or alter the classifier of the specified dependency.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                if (isDependencyTag()) {
                    if (groupId.equals(tag.getChildValue("groupId").orElse(getResolutionResult().getPom().getGroupId())) &&
                            artifactId.equals(tag.getChildValue("artifactId").orElse(null))) {
                        Optional<Xml.Tag> scope = tag.getChild("classifier");
                        if (scope.isPresent()) {
                            if (newClassifier == null) {
                                doAfterVisit(new RemoveContentVisitor<>(scope.get(), false));
                            } else if (!newClassifier.equals(scope.get().getValue().orElse(null))) {
                                doAfterVisit(new ChangeTagValueVisitor<>(scope.get(), newClassifier));
                            }
                        } else if (newClassifier != null) {
                            doAfterVisit(new AddToTagVisitor<>(tag, Xml.Tag.build("<classifier>" + newClassifier + "</classifier>")));
                        }
                    }
                }

                return super.visitTag(tag, ctx);
            }
        };
    }
    
    public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getNewClassifier() {
		return newClassifier;
	}

	public void setNewClassifier(String newClassifier) {
		this.newClassifier = newClassifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(artifactId, groupId, newClassifier);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeDependencyClassifier other = (ChangeDependencyClassifier) obj;
		return Objects.equals(artifactId, other.artifactId) && Objects.equals(groupId, other.groupId)
				&& Objects.equals(newClassifier, other.newClassifier);
	}

}
