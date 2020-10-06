package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springsource.ide.eclipse.commons.core.util.OsUtils;

import com.google.common.collect.ImmutableList;

public class BuildScriptLocator {

	public static enum BuildKind {
		MAVEN,
		GRADLE,
		SHELL
	};

	public final File directory;
	public final List<String> command = new ArrayList<>();
	public final List<File> checkedLocations = new ArrayList<>();
	private BuildKind buildKind;

	/**
	 * Tries to find build script in given directory. Various bits of information
	 * such as the type of script found, the build command to run etc. can
	 * be accessed via the public fields and accessors of the created object.
	 */
	public BuildScriptLocator(File directory) {
		this.directory = directory;
		findBuildScript();
	}

	private void findBuildScript() {
		String buildShellScript;
		this.buildKind = null;
		if (OsUtils.isWindows()) {
			buildShellScript = "sts-docker-build.bat";
			if (exists(directory.toPath().resolve(buildShellScript))) {
				buildKind = BuildKind.SHELL;
				command.addAll(ImmutableList.of("CMD", "/C", buildShellScript));
			} else if (exists(directory.toPath().resolve("mvnw.cmd"))) {
				buildKind = BuildKind.MAVEN;
				command.addAll(ImmutableList.of("CMD", "/C", "mvnw.cmd", "spring-boot:build-image", "-DskipTests"));
				//, "-Dspring-boot.repackage.excludeDevtools=false" };
			} else if (exists(directory.toPath().resolve("gradlew.bat"))) {
				buildKind = BuildKind.GRADLE;
				command.addAll(ImmutableList.of("CMD", "/C", "gradlew.bat", "bootBuildImage", "-x", "test"));
			} 
		} else {
			buildShellScript = "sts-docker-build.sh";
			if (exists(directory.toPath().resolve(buildShellScript))) {
				buildKind = BuildKind.SHELL;
				command.addAll(ImmutableList.of("./sts-docker-build.sh"));
			} else if (exists(directory.toPath().resolve("mvnw"))) {
				buildKind = BuildKind.MAVEN;
				command.addAll(ImmutableList.of("./mvnw", "spring-boot:build-image", "-DskipTests"));
			} else if (exists(directory.toPath().resolve("gradlew"))) {
				buildKind = BuildKind.GRADLE;
				command.addAll(ImmutableList.of("./gradlew", "--stacktrace", "bootBuildImage", "-x", "test" ));
			}	
		}
	}

	private boolean exists(Path resolved) {
		checkedLocations.add(resolved.toFile());
		return Files.exists(resolved);
	}

	/**
	 * @return The type of build script that was found. Returns null to indicate no build script was found.
	 */
	public BuildKind getBuildKind() {
		return this.buildKind;
	}
}
