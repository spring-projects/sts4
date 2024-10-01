package org.test;

import org.springframework.stereotype.Service;

@Service
public class VisitService {
	
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

}
