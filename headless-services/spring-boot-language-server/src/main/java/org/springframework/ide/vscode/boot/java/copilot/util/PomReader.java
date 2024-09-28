package org.springframework.ide.vscode.boot.java.copilot.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Ryan Baxter
 */
public class PomReader {

	/**
	 * Returns a parsed POM.
	 */
	public Model readPom(File file) {
		File pom = file;
		if (file.isDirectory()) {
			pom = new File(file, "pom.xml");
		}
		if (!pom.exists()) {
			return null;
		}
		String fileText = "";
		try (Reader reader = new FileReader(pom)) {
			if (file.isFile()) {
				fileText = new String(Files.readAllBytes(file.toPath()));
			}
			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			return xpp3Reader.read(reader);
		}
		catch (XmlPullParserException | IOException ex) {
			if (file.isFile() && fileText.length() == 0) {
				throw new IllegalStateException("File [" + pom.getAbsolutePath() + "] is empty", ex);
			}
			throw new IllegalStateException("Failed to read file: " + pom.getAbsolutePath(), ex);
		}
	}

}
