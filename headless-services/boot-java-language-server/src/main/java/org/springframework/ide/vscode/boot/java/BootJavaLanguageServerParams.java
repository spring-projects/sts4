package org.springframework.ide.vscode.boot.java;

import java.util.Arrays;

import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectCache;
import org.springframework.ide.vscode.commons.gradle.GradleProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeProjectOvserver;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectCache;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectFinder;
import org.springframework.ide.vscode.commons.util.FileObserver;

public class BootJavaLanguageServerParams {

	public final JavaProjectFinder projectFinder;
	public final ProjectObserver projectObserver;
	public final SpringPropertyIndexProvider indexProvider;
	public final RunningAppProvider runningAppProvider;

	public BootJavaLanguageServerParams(
			JavaProjectFinder projectFinder,
			ProjectObserver projectObserver,
			SpringPropertyIndexProvider indexProvider,
			RunningAppProvider runningAppProvider) {
		super();
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		this.indexProvider = indexProvider;
		this.runningAppProvider = runningAppProvider;
	}

	public static LSFactory<BootJavaLanguageServerParams> createDefault() {
		return (SimpleLanguageServer server) -> {
			// Initialize project finders, project caches and project observers
			FileObserver fileObserver = server.getWorkspaceService().getFileObserver();
			CompositeJavaProjectFinder javaProjectFinder = new CompositeJavaProjectFinder();
			MavenProjectCache mavenProjectCache = new MavenProjectCache(fileObserver, MavenCore.getDefault());
			javaProjectFinder.addJavaProjectFinder(new MavenProjectFinder(mavenProjectCache));

			GradleProjectCache gradleProjectCache = new GradleProjectCache(fileObserver, GradleCore.getDefault());
			javaProjectFinder.addJavaProjectFinder(new GradleProjectFinder(gradleProjectCache));

			CompositeProjectOvserver projectObserver = new CompositeProjectOvserver(Arrays.asList(mavenProjectCache, gradleProjectCache));

			return new BootJavaLanguageServerParams(
					javaProjectFinder,
					projectObserver,
					new DefaultSpringPropertyIndexProvider(javaProjectFinder),
					RunningAppProvider.DEFAULT
			);
		};
	}


}
