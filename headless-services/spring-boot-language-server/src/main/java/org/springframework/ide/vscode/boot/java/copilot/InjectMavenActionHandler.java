package org.springframework.ide.vscode.boot.java.copilot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openrewrite.Recipe;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.maven.MavenParser;
import org.springframework.ide.vscode.boot.java.copilot.recipe.AddDependencyRecipeFactory;
import org.springframework.ide.vscode.boot.java.copilot.recipe.AddManagedDependencyRecipeFactory;
import org.springframework.ide.vscode.boot.java.copilot.recipe.AddPluginRecipeFactory;
import org.springframework.ide.vscode.boot.java.copilot.recipe.InjectTextMavenRepositoryRecipe;
import org.springframework.ide.vscode.boot.java.copilot.util.MavenBuildPluginReader;
import org.springframework.ide.vscode.boot.java.copilot.util.MavenDependencyReader;
import org.springframework.ide.vscode.boot.java.copilot.util.MavenRepositoryReader;

public class InjectMavenActionHandler extends AbstractInjectMavenActionHandler {

	private List<InjectMavenDependency> dependencies;

	private List<InjectMavenBuildPlugin> buildPlugins;

	private List<InjectMavenRepository> repositories;

	private List<InjectMavenDependencyManagement> dependencyManagements;

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

	public boolean injectDependency(InjectMavenDependency dependency) {
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
		MavenParser mavenParser = MavenParser.builder().build();
		for (InjectMavenDependency d : dependencies) {
			String text = getTextToUse(d.getText(), "Inject Maven Dependency");
			MavenDependencyReader mavenDependencyReader = new MavenDependencyReader();
			String[] mavenDependencies = mavenDependencyReader.parseMavenSection(text);
			for (String md : mavenDependencies) {
				aggregateRecipe.getRecipeList().add(new AddDependencyRecipeFactory().create(md));
			}
		}
		for (InjectMavenBuildPlugin p : buildPlugins) {
			String text = getTextToUse(p.getText(), "Inject Maven Build Plugin");
			MavenBuildPluginReader mavenBuildPluginReader = new MavenBuildPluginReader();
			String[] buildPlugins = mavenBuildPluginReader.parseMavenSection(text);
			for (String mp : buildPlugins) {
				aggregateRecipe.getRecipeList().add(new AddPluginRecipeFactory().create(mp));
			}
		}
		for (InjectMavenRepository r : repositories) {
			String text = getTextToUse(r.getText(), "Inject Maven Repository");
			MavenRepositoryReader mavenRepositoryReader = new MavenRepositoryReader();
			String[] mavenRepositories = mavenRepositoryReader.parseMavenSection(text);
			for (String mr : mavenRepositories) {
				aggregateRecipe.getRecipeList().add(new InjectTextMavenRepositoryRecipe(mr));
			}
		}
		for (InjectMavenDependencyManagement dm : dependencyManagements) {
			String text = getTextToUse(dm.getText(), "Inject Maven Dependency Management");
			MavenDependencyReader mavenDependencyReader = new MavenDependencyReader();
			String[] mavenDependencyManagements = mavenDependencyReader.parseMavenSection(text);
			for (String mdm : mavenDependencyManagements) {
				aggregateRecipe.getRecipeList().add(new AddManagedDependencyRecipeFactory().create(mdm));
			}
		}
		return aggregateRecipe;
	}

}
