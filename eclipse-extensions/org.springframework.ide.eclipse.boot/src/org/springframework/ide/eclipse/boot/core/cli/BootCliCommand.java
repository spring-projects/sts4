package org.springframework.ide.eclipse.boot.core.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springsource.ide.eclipse.commons.core.process.OutputWriter;
import org.springsource.ide.eclipse.commons.core.process.ProcessRunner;
import org.springsource.ide.eclipse.commons.core.process.StandardProcessRunner;
import org.springsource.ide.eclipse.commons.core.process.SystemErrOutputWriter;
import org.springsource.ide.eclipse.commons.core.process.SystemOutOutputWriter;

public class BootCliCommand {


	private static final String SCRIPT_NAME = "bin" + File.separatorChar + "spring";

	private static final String UNIX_SUFFIX = "";

	private static final String WINDOWS_SUFFIX = ".bat";

	private final ProcessRunner processRunner;

	private final File script;

	private final OutputWriter writer = new OutputWriter() {

		StringBuffer buffer = new StringBuffer();

		public void write(String line) {
			buffer.append(line + "\n");
		}

		public String toString() {
			return buffer.toString();
		}

	};

	public BootCliCommand(File runtimeDirectory) {
		this.script = new File(runtimeDirectory, SCRIPT_NAME + (isWindows() ? WINDOWS_SUFFIX : UNIX_SUFFIX));
		this.processRunner = new StandardProcessRunner(//
				new OutputWriter[] { new SystemOutOutputWriter(), writer }, //
				new OutputWriter[] { new SystemErrOutputWriter(), writer });
	}

	public String getOutput() {
		return writer.toString();
	}

	public int execute(String... arguments) {
		try {
			if (script != null && script.exists()) {
				script.setExecutable(true);
			}
			return this.processRunner.run(getProcessWorkingFolder(), getProcessArguments(arguments));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String[] getProcessArguments(String... arguments) {
		List<String> allArguments = new ArrayList<>(arguments.length + 1);
		if (isWindows()) {
			allArguments.add("\"" + this.script.getAbsolutePath() + "\"");
		} else {
			allArguments.add(this.script.getAbsolutePath());
		}
		allArguments.addAll(Arrays.asList(arguments));
		return allArguments.toArray(new String[allArguments.size()]);
	}
	
	public File getProcessWorkingFolder() {
		return this.script.getParentFile();
	}

	private boolean isWindows() {
		return File.separatorChar == '\\';
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		if (this.script != null) {
			str.append(this.script.getAbsolutePath());
		}
		return str.toString();
	}

}
