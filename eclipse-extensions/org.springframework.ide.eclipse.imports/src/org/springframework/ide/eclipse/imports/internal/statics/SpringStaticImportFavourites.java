/*******************************************************************************
 * Copyright (c) 2007, 2016 Spring IDE Developers, IBM Corporation, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial public API
 *     IBM Corporation and others - original implementation
 *     
 * Original License for code derived from: org.eclipse.jdt.internal.ui.preferences.CodeAssistFavoritesConfigurationBlock:
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.imports.internal.statics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock.Key;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.imports.ImportsActivator;

/**
 * Loads Types containing statics into Eclipse Static Import favourites
 * preferences.
 * <p/>
 * NOTE: Some code derived from:
 * <p/>
 * org.eclipse.jdt.internal.ui.preferences.CodeAssistFavoritesConfigurationBlock
 * <p/>
 *
 */
public class SpringStaticImportFavourites {

	public static final String SPRING_IDE_IMPORT_STATICS_INSTANCE_SCOPE = ImportsActivator.PLUGIN_ID
			+ ".importStaticsInstanceScope";

	private static final Key PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS = getJDTUIKey(
			PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);

	private static final Key PREF_SPRING_IDE_IMPORT_STATICS_INSTANCE_SCOPE = getSpringIDEImportStaticsKey(
			SPRING_IDE_IMPORT_STATICS_INSTANCE_SCOPE);

	private static final String WILDCARD = ".*";

	private IWorkingCopyManager manager;

	private final StaticImportCatalogue importStaticsCatalogue;

	public SpringStaticImportFavourites(StaticImportCatalogue importStaticsCatalogue) {
		this.importStaticsCatalogue = importStaticsCatalogue;
		manager = new WorkingCopyManager();
	}

	/**
	 * load the static imports into Eclipse favourites preferences
	 * asynchronously
	 */
	public void asynchLoad() {

		Job job = new Job("Loading Spring static imports into Eclipse code assist favourites") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				load();
				return Status.OK_STATUS;
			}
		};

		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	public void load() {

		// Write to defaults separately as to allow "restore to defaults" to
		// work
		writeDefaultScopedImports(this.importStaticsCatalogue);

		// Instance scope is what users edit as their own preferences.
		writeInstanceScopedImports(this.importStaticsCatalogue);
	}

	protected static Key getKey(String plugin, String key) {
		return new Key(plugin, key);
	}

	public String[] getStoredImportStatics(IScopeContext context) {
		String str = PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS.getStoredValue(context, manager);

		if (str != null && str.length() > 0) {
			return deserializeFavorites(str);
		}
		return new String[0];
	}

	private String[] deserializeFavorites(String str) {
		return str.split(";");
	}

	private static String serializeFavorites(List<String> favorites) {
		int size = favorites.size();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			buf.append(favorites.get(i));
			if (i < size - 1) {
				buf.append(';');
			}
		}
		return buf.toString();
	}

	protected static final Key getJDTUIKey(String key) {
		return getKey(JavaUI.ID_PLUGIN, key);
	}

	protected static Key getSpringIDEImportStaticsKey(String key) {
		return getKey(ImportsActivator.PLUGIN_ID, key);
	}

	protected List<String> mergeWithExisting(List<String> favorites, IScopeContext context) {

		String[] existing = getStoredImportStatics(context);
		List<String> merged = new ArrayList<String>(Arrays.asList(existing));
		for (String fav : favorites) {
			if (!merged.contains(fav)) {
				merged.add(fav);
			}
		}
		return merged;
	}

	protected void writeDefaultScopedImports(StaticImportCatalogue catalogue) {
		List<String> validated = getValidated(catalogue.getCatalogue());
		List<String> merged = mergeWithExisting(validated, DefaultScope.INSTANCE);

		new FavouritesPreferenceWriter(merged, manager, DefaultScope.INSTANCE).write();
	}

	protected void writeInstanceScopedImports(StaticImportCatalogue catalogue) {
		// Only write to instance scope ONCE to avoid undoing any changes users
		// make to instance
		// scope preferences on any subsequent restart of STS
		String writtenVal = PREF_SPRING_IDE_IMPORT_STATICS_INSTANCE_SCOPE.getStoredValue(InstanceScope.INSTANCE,
				manager);

		if (writtenVal == null || !Boolean.parseBoolean(writtenVal)) {
			List<String> validated = getValidated(catalogue.getCatalogue());
			List<String> merged = mergeWithExisting(validated, InstanceScope.INSTANCE);
			new FavouritesPreferenceWriter(merged, manager, InstanceScope.INSTANCE).write();

			// Be sure to remember that instance scope was written
			new PreferenceWriter(Boolean.toString(true), PREF_SPRING_IDE_IMPORT_STATICS_INSTANCE_SCOPE, manager,
					InstanceScope.INSTANCE).write();
		}

	}

	protected List<String> getValidated(String[] original) {

		List<String> validated = new ArrayList<String>();
		for (String val : original) {
			IStatus status = JavaConventions.validateJavaTypeName(val, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
			if (status.isOK()) {
				validated.add(asWildCard(val));
			} else {
				ImportsActivator.log(status);
			}
		}

		return validated;
	}

	protected String asWildCard(String val) {
		return val + WILDCARD;
	}

	class FavouritesPreferenceWriter extends PreferenceWriter {

		public FavouritesPreferenceWriter(List<String> values, IWorkingCopyManager manager, IScopeContext context) {
			super(serializeFavorites(values), SpringStaticImportFavourites.PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS,
					manager, context);
		}

	}

	class PreferenceWriter {

		protected final IWorkingCopyManager manager;
		protected final IScopeContext context;
		protected final String values;
		protected final Key key;

		public PreferenceWriter(String values, Key key, IWorkingCopyManager manager, IScopeContext context) {
			this.manager = manager;
			this.context = context;
			this.values = values;
			this.key = key;
		}

		public void write() {
			write(key, values, context);
			applyChanges();
		}

		protected String write(Key key, String value, IScopeContext context) {
			String oldValue = key.getStoredValue(context, manager);
			key.setStoredValue(context, value, manager);
			return oldValue;
		}

		protected void applyChanges() {
			try {
				manager.applyChanges();
			} catch (BackingStoreException e) {
				ImportsActivator.log(e);
			}
		}

	}

}
