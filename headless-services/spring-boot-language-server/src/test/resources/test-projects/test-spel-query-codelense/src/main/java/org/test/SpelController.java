package org.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpelController {

	@Value("${app.version}")
	private String appVersion;

	@Value(value="#{'${app.version}' matches '\\\\d+\\\\.\\\\d+\\\\.\\\\d+' ? '${app.version}' : 'Invalid Version'}")
	private String version;

	@Value("#{T(org.test.SpelController).isValidVersion('${app.version}') ? 'Valid Version' :'Invalid Version'}")
	private String versionValidity;
	
	@Value("#{T(org.test.SpelController).toUpperCase('hello') + ' ' + T(org.test.SpelController).concat('world', '!')}")
    private String greeting;

	@GetMapping("/version")
	@ResponseBody
	public String getAppVersionInfo() {
		return "Version: " + appVersion + ", Version Validity: " + version;
	}

	@GetMapping("/validateVersion")
	@ResponseBody
	public String validateVersion() {
		return "Version: " + appVersion + ", Version Validity: " + versionValidity;
	}

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
