/******************************************************************************
 * Copyright (c) 2006, 2013 VMware Inc., Oracle Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * https://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at https://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *   Oracle Inc.
 *****************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.internal;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Bundle;

/**
 * ClassLoader backed by an OSGi bundle. Provides the ability to use a separate
 * class loader as fall back.
 * 
 * Contains facilities for tracing class loading behaviour so that issues can be
 * easily resolved.
 * 
 * For debugging please see {@link DebugUtils}.
 * 
 * This class is copied and modified from the original source code from the
 * Eclipse Gemini Blueprint project.
 * 
 * @author Adrian Colyer
 * @author Andy Piper
 * @author Costin Leau
 * @author Martin Lippert
 */
public class BundleDelegatingClassLoader extends ClassLoader {

	private static final String NULL_STRING = "null";

	private static final Enumeration<URL> EMPTY_RESOURCES = new Enumeration<URL>() {
        public boolean hasMoreElements() {
            return false;
        }
        public URL nextElement() {
            throw new NoSuchElementException();
        }
    };

    /**
     * Transparently enumerates across two enumerations.
     */
    private static class CombinedEnumeration<T> implements Enumeration<T> {
        private final Enumeration<T> e1;
        private final Enumeration<T> e2;

        public CombinedEnumeration(Enumeration<T> e1, Enumeration<T> e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        public boolean hasMoreElements() {
            return e1.hasMoreElements() || e2.hasMoreElements();
        }

        public T nextElement() {
            if (e1.hasMoreElements()) {
                return e1.nextElement();
            }
            if (e2.hasMoreElements()) {
                return e2.nextElement();
            }

            throw new NoSuchElementException();
        }
    }

	private final ClassLoader bridge;

	private final Bundle backingBundle;


	/**
	 * Factory method for creating a class loader over the given bundle.
	 * 
	 * @param aBundle bundle to use for class loading and resource acquisition
	 * @return class loader adapter over the given bundle
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(Bundle aBundle) {
		return createBundleClassLoaderFor(aBundle, null);
	}

	/**
	 * Factory method for creating a class loader over the given bundle and with
	 * a given class loader as fall-back. In case the bundle cannot find a class
	 * or locate a resource, the given class loader will be used as fall back.
	 * 
	 * @param bundle bundle used for class loading and resource acquisition
	 * @param bridge class loader used as fall back in case the bundle cannot
	 *        load a class or find a resource. Can be <code>null</code>
	 * @return class loader adapter over the given bundle and class loader
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
		return AccessController.doPrivileged(new PrivilegedAction<BundleDelegatingClassLoader>() {

			public BundleDelegatingClassLoader run() {
				return new BundleDelegatingClassLoader(bundle, bridge);
			}
		});
	}

	/**
	 * Private constructor.
	 * 
	 * Constructs a new <code>BundleDelegatingClassLoader</code> instance.
	 * 
	 * @param bundle
	 * @param bridgeLoader
	 */
	protected BundleDelegatingClassLoader(Bundle bundle, ClassLoader bridgeLoader) {
		super(null);
		Assert.isNotNull(bundle, "bundle should be non-null");
		this.backingBundle = bundle;
		this.bridge = bridgeLoader;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return this.backingBundle.loadClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]",
				cnfe);
		}
		catch (NoClassDefFoundError ncdfe) {
			// This is almost always an error
			// This is caused by a dependent class failure,
			// so make sure we search for the right one.
			NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle ["
					+ nullSafeNameAndSymName(backingBundle) + "]");
			e.initCause(ncdfe);
			throw e;
		}
	}

	@Override
	protected URL findResource(String name) {
		URL url = this.backingBundle.getResource(name);
		return url;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		Enumeration<URL> enm = this.backingBundle.getResources(name);
		return enm;
	}

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = this.backingBundle.getResources(name);

        if (this.bridge != null) {
            Enumeration<URL> bridgeResources = this.bridge.getResources(name);
            if (resources == null) {
                resources = bridgeResources;
            } else if (bridgeResources != null){
                resources = new CombinedEnumeration<URL>(resources, bridgeResources);
            }
        }

        // Classloader contract: Never return null but rather an empty enumeration.
        return resources != null ? resources : EMPTY_RESOURCES;
    }

    @Override
	public URL getResource(String name) {
		URL resource = findResource(name);
		if (bridge != null && resource == null) {
			resource = bridge.getResource(name);
		}
		return resource;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;
		try {
			clazz = findClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			if (bridge != null)
				clazz = bridge.loadClass(name);
			else
				throw cnfe;
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}

	@Override
	public String toString() {
		return "BundleDelegatingClassLoader for [" + nullSafeNameAndSymName(backingBundle) + "]";
	}

	/**
	 * Returns the bundle to which this class loader delegates calls to.
	 * 
	 * @return the backing bundle
	 */
	public Bundle getBundle() {
		return backingBundle;
	}
	
	protected String nullSafeNameAndSymName(Bundle bundle) {
		if (bundle == null)
			return NULL_STRING;

		Dictionary<String, String> dict = bundle.getHeaders();

		if (dict == null)
			return NULL_STRING;

		StringBuilder buf = new StringBuilder();
		String name = dict.get(org.osgi.framework.Constants.BUNDLE_NAME);
		if (name == null)
			buf.append(NULL_STRING);
		else
			buf.append(name);
		buf.append(" (");
		String sname = dict.get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);

		if (sname == null)
			buf.append(NULL_STRING);
		else
			buf.append(sname);

		buf.append(")");

		return buf.toString();
	}

}