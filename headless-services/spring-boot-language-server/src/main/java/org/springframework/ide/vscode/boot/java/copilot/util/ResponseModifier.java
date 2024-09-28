package org.springframework.ide.vscode.boot.java.copilot.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ResponseModifier {

	public String modify(String response) {
		return modifyMsyqlDependency(modifyJavax(response));
	}

	private String modifyJavax(String response) {
		StringBuilder sb = new StringBuilder();
		sb.append("*Note: The code provided is just an example and may not be suitable for production use.*");
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
		sb.append("Generated on " + LocalDateTime.now().format(formatter));
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append(response);
		String code = sb.toString();

		if (!code.contains("import javax")) {
			return code;
		}
		return replaceImportStatements(code);
	}

	private static String replaceImportStatements(String content) {
		String[] lines = content.split(System.lineSeparator());
		StringBuilder updatedContent = new StringBuilder();

		for (String line : lines) {
			if (line.trim().startsWith("import ")) {
				String packageName = extractPackageName(line);
				if (!packageName.contains("javax.sql.")) {
					packageName = packageName.replace("javax", "jakarta");
				}
				line = "import " + packageName + ";";
			}
			updatedContent.append(line).append(System.lineSeparator());
		}
		return updatedContent.toString();
	}

	private static String extractPackageName(String importStatement) {
		return importStatement.replace("import", "").replace(";", "").trim();
	}

	private String modifyMsyqlDependency(String response) {
		if (!response.contains("<artifactId>mysql-connector-java</artifactId>")) {
			return response;
		} else {
			String s1 = response.replace("<groupId>mysql</groupId>", "<groupId>com.mysql</groupId>");
			String s2 = s1.replace("<artifactId>mysql-connector-java</artifactId>",
					"<artifactId>mysql-connector-j</artifactId>");
			return s2;
		}

	}

}
