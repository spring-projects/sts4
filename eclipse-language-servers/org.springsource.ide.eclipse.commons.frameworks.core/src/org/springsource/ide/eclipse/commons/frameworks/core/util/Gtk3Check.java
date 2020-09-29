/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Kris De Volder - Initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;

public class Gtk3Check {

	//Related eclipse bug:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434869
	//If this bug is fixed before eclipse 4.4 release we can remove this code
	//and any references to it

	public static final boolean isGTK3 = isGTK3();

	/**
	 * Try to determine if GTK3 native libraries are loaded. If it can't be determined conclusively
	 * assume 'the worst' case and return true.
	 */
	private static boolean isGTK3() {

		if (!"linux".equals(Platform.getOS())) {
			return false;
		}
		try {
			//Note don't look in the 'swt.gtk' bundle because it is a fragment.
			// Should look in the 'host' bundle instead.
			Bundle bundle = Platform.getBundle("org.eclipse.swt");
			BundleWiring bw = bundle.adapt(BundleWiring.class);
			ClassLoader cl = bw.getClassLoader();
			List<String> libs = getNativeLibs(cl);
			for (String lib : libs) {
//				System.out.println(lib);
				//TODO: not sure how good this check is. Could library exist under a different name?
				if (lib.contains("swt-pi3-gtk")) {
					return true;
				}
			}
			//No problems determining loaded libs, and gtk3 lib not found!
			return false;
		} catch (Throwable e) {
			FrameworkCoreActivator.log(e);
			//Something went wrong determining if GTK3 is present.
			//We must assume the worst to avoid potential JVM crash.
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	private static Vector<String> getNativeLibs(ClassLoader classLoader) throws Exception {
		//See: https://stackoverflow.com/questions/1007861/how-do-i-get-a-list-of-jni-libraries-which-are-loaded
		Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
		field.setAccessible(true);
		return (Vector<String>)field.get(classLoader);
	}

}
