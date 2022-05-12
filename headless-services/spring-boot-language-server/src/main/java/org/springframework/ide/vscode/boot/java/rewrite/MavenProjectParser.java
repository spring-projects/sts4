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
package org.springframework.ide.vscode.boot.java.rewrite;

import static org.openrewrite.Tree.randomId;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.marker.JavaVersion;
import org.openrewrite.marker.BuildTool;
import org.openrewrite.marker.Marker;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.maven.tree.Dependency;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.maven.tree.Pom;
import org.openrewrite.maven.tree.ResolvedPom;
import org.openrewrite.properties.PropertiesParser;
import org.openrewrite.xml.XmlParser;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Document;
import org.openrewrite.yaml.YamlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse a Maven project on disk into a list of {@link org.openrewrite.SourceFile} including
 * Maven, Java, YAML, properties, and XML AST representations of sources and resources found.
 */
public class MavenProjectParser {

    private static final Pattern mavenWrapperVersionPattern = Pattern.compile(".*apache-maven/(.*?)/.*");
    private static final Logger logger = LoggerFactory.getLogger(MavenProjectParser.class);

    private final MavenParser mavenParser;
    private final JavaParser.Builder<?, ?> javaParserBuilder;
    private final ExecutionContext ctx;

    public MavenProjectParser(MavenParser.Builder mavenParserBuilder,
                              JavaParser.Builder<?, ?> javaParserBuilder,
                              ExecutionContext ctx) {
        this.mavenParser = mavenParserBuilder.build();
        this.javaParserBuilder = javaParserBuilder;
        this.ctx = ctx;
    }

    /**
     * Given a root path to a maven project, this parser will parse the maven project (including submodules)
     * and return a list of ALL source files for all maven modules under the root path.
     * <PRE>
     * Notes About Provenance Information:
     *
     * There are always three markers applied to each source file and there can potentially be up to five provenance
     * markers in total:
     *
     * BuildTool     - What build tool was used to compile the source file (This will always be Maven)
     * JavaVersion   - What Java version/vendor was used when compiling the source file.
     * JavaProject   - For each maven module/sub-module, the same JavaProject will be associated with ALL source files
     *                 belonging to that module.
     *
     * Optional:
     *
     * GitProvenance - If the entire project exists in the context of a git repository, all source files (for all modules) will have the same GitProvenance.
     * JavaSourceSet - All Java source files and all resource files that exist in src/main or src/test will have a JavaSourceSet marker assigned to them.
     *
     * </PRE>
     * @param projectDirectory A path to the root folder containing a meven project.
     * @return A list of source files that have been parsed from the root folder
     */
    public List<SourceFile> parse(Path projectDirectory, List<Path> dependencies) {
        List<Xml.Document> mavens = mavenParser.parse(getMavenPoms(projectDirectory, ctx), projectDirectory, ctx);
        List<Document> sorted = sort(mavens);
        
        // Filter out pom files inside target folders. (Naive implementation.)
        mavens = sorted.stream().filter(m -> !isInsideBuildFolderOfOtherMavenProjects(sorted, m)).collect(Collectors.toList());

        JavaParser javaParser = javaParserBuilder.build();

        logger.info("The order in which projects are being parsed is:");
        for (Xml.Document maven : mavens) {
            logger.info("  {}:{}", getModel(maven).getGroupId(), getModel(maven).getArtifactId());
        }

        List<SourceFile> sourceFiles = new ArrayList<>();
        for (Xml.Document maven : mavens) {
            List<Marker> projectProvenance = getJavaProvenance(maven, projectDirectory);
            sourceFiles.add(addProjectProvenance(maven, projectProvenance));

//            List<Path> dependencies = downloadArtifacts(getResolvedPom(maven).getDependencies().get(Scope.Compile));
            javaParser.setSourceSet("main");
            javaParser.setClasspath(dependencies);
            sourceFiles.addAll(ListUtils.map(javaParser.parse(
            		getJavaSources(getModel(maven).getRequested(), projectDirectory, ctx), projectDirectory, ctx), addProvenance(projectProvenance)));
            //Resources in the src/main should also have the main source set attached to them.
            parseResources(getResources(getModel(maven).getRequested(), projectDirectory, ctx), projectDirectory, sourceFiles, projectProvenance, javaParser.getSourceSet(ctx));

//            List<Path> testDependencies = downloadArtifacts(maven.getModel().getDependencies(Scope.Test));
            javaParser.setSourceSet("test");
//            javaParser.setClasspath(testDependencies);
            sourceFiles.addAll(ListUtils.map(javaParser.parse(
                    getTestJavaSources(getModel(maven).getRequested(), projectDirectory, ctx), projectDirectory, ctx), addProvenance(projectProvenance)));
            //Resources in the src/test should also have the test source set attached to them.
            parseResources(getTestResources(getModel(maven).getRequested(), projectDirectory, ctx), projectDirectory, sourceFiles, projectProvenance, javaParser.getSourceSet(ctx));
        }

        return sourceFiles;
    }

    private List<Marker> getJavaProvenance(Xml.Document maven, Path projectDirectory) {
        ResolvedPom mavenModel = getModel(maven);
        String javaRuntimeVersion = System.getProperty("java.runtime.version");
        String javaVendor = System.getProperty("java.vm.vendor");
        String sourceCompatibility = javaRuntimeVersion;
        String targetCompatibility = javaRuntimeVersion;
        String propertiesSourceCompatibility = mavenModel.getValue(mavenModel.getValue("maven.compiler.source"));
        if (propertiesSourceCompatibility != null) {
            sourceCompatibility = propertiesSourceCompatibility;
        }
        String propertiesTargetCompatibility = mavenModel.getValue(mavenModel.getValue("maven.compiler.target"));
        if (propertiesTargetCompatibility != null) {
            targetCompatibility = propertiesTargetCompatibility;
        }

        Path wrapperPropertiesPath = projectDirectory.resolve(".mvn/wrapper/maven-wrapper.properties");
        String mavenVersion = "3.6";
        if (Files.exists(wrapperPropertiesPath)) {
            try {
                Properties wrapperProperties = new Properties();
                wrapperProperties.load(new FileReader(wrapperPropertiesPath.toFile()));
                String distributionUrl = (String) wrapperProperties.get("distributionUrl");
                if (distributionUrl != null) {
                    Matcher wrapperVersionMatcher = mavenWrapperVersionPattern.matcher(distributionUrl);
                    if (wrapperVersionMatcher.matches()) {
                        mavenVersion = wrapperVersionMatcher.group(1);
                    }
                }
            } catch (IOException e) {
                ctx.getOnError().accept(e);
            }
        }

        return Arrays.asList(
                new BuildTool(randomId(), BuildTool.Type.Maven, mavenVersion),
                new JavaVersion(randomId(), javaRuntimeVersion, javaVendor, sourceCompatibility, targetCompatibility),
                new JavaProject(randomId(), mavenModel.getRequested().getName(), new JavaProject.Publication(
                        mavenModel.getGroupId(),
                        mavenModel.getArtifactId(),
                        mavenModel.getVersion()
                ))
        );
    }

    private void parseResources(List<Path> resources, Path projectDirectory, List<SourceFile> sourceFiles, List<Marker> projectProvenance, JavaSourceSet sourceSet) {
        List<Marker> provenance = new ArrayList<>(projectProvenance);
        provenance.add(sourceSet);

        sourceFiles.addAll(ListUtils.map(new XmlParser().parse(
                resources.stream()
                        .filter(p -> p.getFileName().toString().endsWith(".xml") ||
                                p.getFileName().toString().endsWith(".wsdl") ||
                                p.getFileName().toString().endsWith(".xhtml") ||
                                p.getFileName().toString().endsWith(".xsd") ||
                                p.getFileName().toString().endsWith(".xsl") ||
                                p.getFileName().toString().endsWith(".xslt"))
                        .collect(Collectors.toList()),
                projectDirectory,
                ctx
        ), addProvenance(provenance)));

        sourceFiles.addAll(ListUtils.map(new YamlParser().parse(
                resources.stream()
                        .filter(p -> p.getFileName().toString().endsWith(".yml") || p.getFileName().toString().endsWith(".yaml"))
                        .collect(Collectors.toList()),
                projectDirectory,
                ctx
        ), addProvenance(provenance)));

        sourceFiles.addAll(ListUtils.map(new PropertiesParser().parse(
                resources.stream()
                        .filter(p -> p.getFileName().toString().endsWith(".properties"))
                        .collect(Collectors.toList()),
                projectDirectory,
                ctx
        ), addProvenance(provenance)));
    }

    private <S extends SourceFile> S addProjectProvenance(S s, List<Marker> projectProvenance) {
        for (Marker marker : projectProvenance) {
            s = s.withMarkers(s.getMarkers().addIfAbsent(marker));
        }
        return s;
    }

    private <S extends SourceFile> UnaryOperator<S> addProvenance(List<Marker> projectProvenance) {
        return s -> {
            s = addProjectProvenance(s, projectProvenance);
            return s;
        };
    }

//    private List<Path> downloadArtifacts(Set<Dependency> dependencies) {
//        return dependencies.stream()
//                .filter(d -> d.getRepository() != null)
//                .map(artifactDownloader::downloadArtifact)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }

    public static List<Xml.Document> sort(List<Xml.Document> mavens) {
        // the value is the set of maven projects that depend on the key
        Map<Xml.Document, Set<Xml.Document>> byDependedOn = new HashMap<>();

        for (Xml.Document maven : mavens) {
            byDependedOn.computeIfAbsent(maven, m -> new HashSet<>());
            for (Dependency dependency : getModel(maven).getRequested().getDependencies()) {
                for (Xml.Document test : mavens) {
                    if (getModel(test).getGroupId().equals(dependency.getGroupId()) &&
                            getModel(test).getArtifactId().equals(dependency.getArtifactId())) {
                        byDependedOn.computeIfAbsent(maven, m -> new HashSet<>()).add(test);
                    }
                }
            }
        }

        List<Xml.Document> sorted = new ArrayList<>(mavens.size());
        next:
        while (!byDependedOn.isEmpty()) {
            for (Map.Entry<Xml.Document, Set<Xml.Document>> mavenAndDependencies : byDependedOn.entrySet()) {
                if (mavenAndDependencies.getValue().isEmpty()) {
                	Xml.Document maven = mavenAndDependencies.getKey();
                    byDependedOn.remove(maven);
                    sorted.add(maven);
                    for (Set<Xml.Document> dependencies : byDependedOn.values()) {
                        dependencies.remove(maven);
                    }
                    continue next;
                }
            }
        }

        return sorted;
    }
    
    private static boolean isInsideBuildFolderOfOtherMavenProjects(List<Xml.Document> all, Xml.Document current) {
    	return all.stream().filter(m -> {
    		if (m != current) {
    			Path pomPath = m.getSourcePath();
    			return current.getSourcePath().startsWith((pomPath.getParent() == null ? Paths.get("") : pomPath.getParent()) .resolve("target"));
    		}
    		return false;
    	}).findFirst().isPresent();
    }
    
    private static List<Path> getSources(Path srcDir, ExecutionContext ctx, String... fileTypes) {
        if (!srcDir.toFile().exists()) {
            return List.of();
        }

        BiPredicate<Path, java.nio.file.attribute.BasicFileAttributes> predicate = (p, bfa) ->
                bfa.isRegularFile() && Arrays.stream(fileTypes).anyMatch(type -> p.getFileName().toString().endsWith(type));
        try {
            return Files.find(srcDir, 999, predicate).collect(Collectors.toList());
        } catch (IOException e) {
            ctx.getOnError().accept(e);
            return List.of();
        }
    }
    
    public static List<Path> getMavenPoms(Path projectDir, ExecutionContext ctx) {
        return getSources(projectDir, ctx, "pom.xml").stream()
                .filter(p -> p.getFileName().toString().equals("pom.xml") &&
                        !p.toString().contains("/src/"))
                .collect(Collectors.toList());
    }
    
    private static ResolvedPom getModel(Xml.Document maven) {
    	MavenResolutionResult pom = getResolvedPom(maven);
    	return pom == null ? null : pom.getPom();
    }
    
    private static MavenResolutionResult getResolvedPom(Xml.Document maven) {
    	return maven.getMarkers().findFirst(MavenResolutionResult.class).orElse(null);
    }
    
    private static List<Path> getJavaSources(Pom pom, Path projectDir, ExecutionContext ctx) {
        if (pom.getPackaging() != null && !"jar".equals(pom.getPackaging()) && !"bundle".equals(pom.getPackaging())) {
            return List.of();
        }
        return getSources(projectDir.resolve(pom.getSourcePath()).getParent().resolve(Paths.get("src", "main", "java")),
                ctx, ".java");
    }
    
    private static List<Path> getTestJavaSources(Pom pom, Path projectDir, ExecutionContext ctx) {
        if (pom.getPackaging() != null && !"jar".equals(pom.getPackaging()) && !"bundle".equals(pom.getPackaging())) {
            return List.of();
        }
        return getSources(projectDir.resolve(pom.getSourcePath()).getParent().resolve(Paths.get("src", "test", "java")),
                ctx, ".java");
    }

    private static List<Path> getResources(Pom pom, Path projectDir, ExecutionContext ctx) {
        if (pom.getPackaging() != null && !"jar".equals(pom.getPackaging()) && !"bundle".equals(pom.getPackaging())) {
            return List.of();
        }
        return getSources(projectDir.resolve(pom.getSourcePath()).getParent().resolve(Paths.get("src", "main", "resources")),
                ctx, ".properties", ".xml", ".yml", ".yaml");
    }
    
    private static List<Path> getTestResources(Pom pom, Path projectDir, ExecutionContext ctx) {
        if (pom.getPackaging() != null && !"jar".equals(pom.getPackaging()) && !"bundle".equals(pom.getPackaging())) {
            return List.of();
        }
        return getSources(projectDir.resolve(pom.getSourcePath()).getParent().resolve(Paths.get("src", "test", "resources")),
                ctx, ".properties", ".xml", ".yml", ".yaml");
    }


}