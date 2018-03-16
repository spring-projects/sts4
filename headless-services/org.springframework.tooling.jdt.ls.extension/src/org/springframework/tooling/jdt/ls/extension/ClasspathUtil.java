package org.springframework.tooling.jdt.ls.extension;

import static org.springframework.tooling.jdt.ls.extension.Logger.log;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.springframework.tooling.jdt.ls.extension.Classpath.CPE;
import static org.springframework.tooling.jdt.ls.extension.Classpath.*;

public class ClasspathUtil {

	public static Classpath resolve(IJavaProject javaProject) throws Exception {

		List<CPE> cpEntries = new ArrayList<>();
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);

		if (entries != null) {
			for (IClasspathEntry entry : entries) {
				String kind = toContentKind(entry);
				String path = entry.getPath().toString();
				cpEntries.add(new CPE(kind, path));
			}
		}
		Classpath classpath = new Classpath(cpEntries, javaProject.getOutputLocation().toString());
		log("classpath=" + classpath.getEntries().size() + " entries");
		return classpath;
	}

	private static String toContentKind(IClasspathEntry entry) {
		switch (entry.getContentKind()) {
		case IPackageFragmentRoot.K_BINARY:
			return ENTRY_KIND_BINARY;
		case IPackageFragmentRoot.K_SOURCE:
			return ENTRY_KIND_SOURCE;
		default:
			return "unknown: " + entry.getContentKind();
		}
	}
}
