package org.springframework.ide.vscode.boot.java.copilot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.maven.AddDependency;
import org.openrewrite.maven.AddManagedDependency;
import org.openrewrite.maven.AddPlugin;
import org.openrewrite.maven.AddRepository;
import org.openrewrite.xml.XmlParser;
import org.openrewrite.xml.search.FindTags;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Document;
import org.openrewrite.xml.tree.Xml.Tag;

public class InjectMavenActionHandler extends AbstractInjectMavenActionHandler {

	private List<MavenDependencyMetadata> dependencies;

	private List<InjectMavenBuildPlugin> buildPlugins;

	private List<InjectMavenRepository> repositories;

	private List<InjectMavenDependencyManagement> dependencyManagements;

	public record MavenDependencyMetadata(String groupId, String artifactId, String version, String scope, String type,
			String classifier) {};

	public record MavenPluginMetadata(String groupId, String artifactId, String version, String configuration,
			String dependencies, String executions, String filePattern) {};

	public record MavenRepositoryMetadata(String id, String url, String repoName, boolean snapshotsEnabled,
			boolean releasesEnabled) {};

	public InjectMavenActionHandler(TemplateEngine templateEngine, Map<String, Object> model, Path cwd) {
		super(templateEngine, model, cwd);
		this.dependencies = new ArrayList<>();
		this.buildPlugins = new ArrayList<>();
		this.repositories = new ArrayList<>();
		this.dependencyManagements = new ArrayList<>();
	}

	public boolean injectBuildPlugin(InjectMavenBuildPlugin buildPlugin) {
		return buildPlugins.add(buildPlugin);
	}

	public boolean injectDependency(MavenDependencyMetadata dependency) {
		return dependencies.add(dependency);
	}

	public boolean injectRepository(InjectMavenRepository repository) {
		return repositories.add(repository);
	}

	public boolean injectDependencyManagement(InjectMavenDependencyManagement dependencyManagement) {
		return dependencyManagements.add(dependencyManagement);
	}

	protected Recipe createRecipe() {
		DeclarativeRecipe aggregateRecipe = new DeclarativeRecipe("spring.cli.ai.MavenUpdates",
				"Add Pom changes from AI", "", Collections.emptySet(), null, null, false, Collections.emptyList());
		for (MavenDependencyMetadata dep : dependencies) {
				if (dep != null) {
					AddDependency addDependency = new AddDependency(dep.groupId(), dep.artifactId(), dep.version(), null,
							dep.scope(), null, null, dep.type(), dep.classifier(), null, null, null);
					aggregateRecipe.getRecipeList().add(addDependency);
				}
		}
		for (InjectMavenBuildPlugin p : buildPlugins) {
			List<Xml.Document> xmlDocuments = parseToXml(p.getText());
			for (Xml.Document xmlDocument : xmlDocuments) {
				for (MavenPluginMetadata pm : findMavenPluginTags(xmlDocument)) {
					AddPlugin addPlugin = new AddPlugin(pm.groupId(), pm.artifactId(), pm.version(), pm.configuration(),
							pm.dependencies(), pm.executions(), pm.filePattern());
					aggregateRecipe.getRecipeList().add(addPlugin);
				}
			}
		}
		for (InjectMavenRepository r : repositories) {
			List<Xml.Document> xmlDocuments = parseToXml(r.getText());
			for (Xml.Document xmlDocument : xmlDocuments) {
				for (MavenRepositoryMetadata rm  : findRepositoryTags(xmlDocument)) {
					AddRepository addRepository = new AddRepository(rm.id(), rm.url(), rm.repoName(), null,
							rm.snapshotsEnabled(), null, null, rm.releasesEnabled(), null, null, null);
					aggregateRecipe.getRecipeList().add(addRepository);
				}
			}
		}
		for (InjectMavenDependencyManagement dm : dependencyManagements) {
			List<Xml.Document> xmlDocuments = parseToXml(dm.getText());
			for (Xml.Document xmlDocument : xmlDocuments) {
				for (MavenDependencyMetadata mdm : findMavenDependencyTags(xmlDocument)) {
					AddManagedDependency addManagedDependency = new AddManagedDependency(mdm.groupId(), mdm.artifactId(),
							mdm.version(), mdm.scope(), null, mdm.classifier(), null, null, null, null);
					aggregateRecipe.getRecipeList().add(addManagedDependency);
				}
			}
		}
		return aggregateRecipe;
	}

	public List<Xml.Document> parseToXml(String content) {
		XmlParser parser = new XmlParser();
		List<SourceFile> sourceFiles = parser.parse(content).collect(Collectors.toList());
		List<Xml.Document> xmlDocuments = sourceFiles.stream().filter(sourceFile -> sourceFile instanceof Xml.Document)
				.map(sourceFile -> (Xml.Document) sourceFile).collect(Collectors.toList());
		return xmlDocuments;
	}

	public List<MavenDependencyMetadata> findMavenDependencyTags(Xml.Document xmlDocument) {
		Set<Tag> dependencyTags = FindTags.find(xmlDocument, "//dependency");
		List<MavenDependencyMetadata> deps = new ArrayList<>(dependencyTags.size());
		for (Tag dependencyTag : dependencyTags) {
			String groupId = dependencyTag.getChildValue("groupId").orElse(null);
			String artifactId = dependencyTag.getChildValue("artifactId").orElse(null);
			String version = dependencyTag.getChildValue("version").orElse("latest");
			String scope = dependencyTag.getChildValue("scope").orElse(null);
			String type = dependencyTag.getChildValue("type").orElse(null);
			String classifier = dependencyTag.getChildValue("classifier").orElse(null);
			if (groupId != null && artifactId != null && version != null) {
				deps.add(new MavenDependencyMetadata(groupId, artifactId, version, scope, type, classifier));
			}
		}
		return deps;
	}

	public List<MavenPluginMetadata> findMavenPluginTags(Xml.Document xmlDocument) {
		Set<Tag> pluginTags = FindTags.find(xmlDocument, "//plugin");
		List<MavenPluginMetadata> plugins = new ArrayList<>(pluginTags.size());
		for (Tag pluginTag : pluginTags) {
			String groupId = pluginTag.getChildValue("groupId").orElse(null);
			String artifactId = pluginTag.getChildValue("artifactId").orElse(null);
			String version = pluginTag.getChildValue("version").orElse("latest");
			String configuration = pluginTag.getChildValue("configuration").orElse(null);
			String dependencies = pluginTag.getChildValue("dependencies").orElse(null);
			String executions = pluginTag.getChildValue("executions").orElse(null);
			String filePattern = pluginTag.getChildValue("filePattern").orElse(null);

			if (groupId != null && artifactId != null && version != null) {
				plugins.add(new MavenPluginMetadata(groupId, artifactId, version, configuration, dependencies, executions,
						filePattern));
			}
		}
		return plugins;
	}

	private List<MavenRepositoryMetadata> findRepositoryTags(Document xmlDocument) {
		Set<Tag> repoTags = FindTags.find(xmlDocument, "//plugin");
		List<MavenRepositoryMetadata> repos = new ArrayList<>(repoTags.size());
		for (Tag repoTag : repoTags) {
			String id = repoTag.getChildValue("id").orElse(null);
			String url = repoTag.getChildValue("url").orElse(null);
			String repoName = repoTag.getChildValue("repoName").orElse("latest");
			boolean snapshotsEnabled = Boolean.parseBoolean(repoTag.getChildValue("snapshotsEnabled").orElse(null));
			boolean releasesEnabled = Boolean.parseBoolean(repoTag.getChildValue("releasesEnabled").orElse(null));

			if (id != null && url != null) {
				repos.add(new MavenRepositoryMetadata(id, url, repoName, snapshotsEnabled, releasesEnabled));
			}
		}
		return repos;
	}

}
