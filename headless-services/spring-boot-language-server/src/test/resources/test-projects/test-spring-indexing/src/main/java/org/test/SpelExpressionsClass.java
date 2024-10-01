package org.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpelExpressionsClass {
	
	@Value("${app.version}")
	private String appVersion;

	@Value("#{@visitController.isValidVersion('${app.version}') ? 'Valid Version' :'Invalid Version'}")
	private String versionValidity;

	@Value("value = #{@visitController.isValidVersion('${app.version}') ? @spelExpressionClass.toUpperCase('valid') :@spelExpressionClass.text2('invalid version)}")
	private String fetchVersion;
	
	@Value("#{T(org.test.SpelExpressionClass).toUpperCase('hello') + ' ' + @spelExpressionsClass.concat('world', '!')}")
	private String greeting;

	public static boolean isValidVersion(String version) {
		if (version.matches("\\d+\\.\\d+\\.\\d+")) {
			String[] parts = version.split("\\.");
			int major = Integer.parseInt(parts[0]);
			int minor = Integer.parseInt(parts[1]);
			int patch = Integer.parseInt(parts[2]);
			return (major > 3) || (major == 3 && (minor > 0 || (minor == 0 && patch >= 0)));
		}
		return false;
	}

	public static String toUpperCase(String input) {
		return input.toUpperCase();
	}

	public static String concat(String str1, String str2) {
		return str1 + str2;
	}

}
