package org.springframework.ide.vscode.commons.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;

public class MavenBuilder {
	
		
	private Path projectPath;

	private List<String> targets;

	private List<String> properties;

	public void execute() throws IOException, InterruptedException {
		Path mvnwPath = System.getProperty("os.name").toLowerCase().startsWith("win") ? projectPath.resolve("mvnw.cmd")
				: projectPath.resolve("mvnw");
		mvnwPath.toFile().setExecutable(true);
		List<String> all = new ArrayList<>(1 + targets.size() + properties.size());
		all.add(mvnwPath.toAbsolutePath().toString());
		all.addAll(targets);
		all.addAll(properties);
		ExternalProcess process = new ExternalProcess(projectPath.toFile(),
				new ExternalCommand(all.toArray(new String[all.size()])), true);
		if (process.getExitValue() != 0) {
			throw new RuntimeException("Failed to build test project! " + process.getErr());
		}
	}
		
	public static MavenBuilder newBuilder(Path projectPath) {
		return new MavenBuilder(projectPath);
	}
	
	public MavenBuilder clean() {
		targets.add("clean");
		return this;
	}
	
	public MavenBuilder pack() {
		targets.add("package");
		return this;
	}
	
	public MavenBuilder skipTests() {
		properties.add("-DskipTests");
		return this;
	}
	
	public MavenBuilder javadoc() {
		properties.add("javadoc:javadoc");
		return this;
	}
	
	private MavenBuilder(Path projectPath) {
		this.projectPath = projectPath;
		this.targets = new ArrayList<>();
		this.properties = new ArrayList<>();
	}

}
