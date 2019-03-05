/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.util.UriUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;

/**
 * @author Martin Lippert
 */
public class SymbolCacheOnDisc implements SymbolCache {

	private final File cacheDirectory;
	private final Map<SymbolCacheKey, CacheStore> stores;

	private static final Logger log = LoggerFactory.getLogger(SymbolCacheOnDisc.class);

	public SymbolCacheOnDisc() throws Exception {
		this(new File(System.getProperty("user.home") + File.separatorChar + ".sts4" + File.separatorChar + ".symbolCache"));
	}

	public SymbolCacheOnDisc(File cacheDirectory) throws Exception {
		this.cacheDirectory = cacheDirectory;
		this.stores = new ConcurrentHashMap<>();

		if (!this.cacheDirectory.exists()) {
			this.cacheDirectory.mkdirs();
		}

		if (!this.cacheDirectory.exists()) {
			throw new Exception("symbol cache directory could not be created:");
		}
	}

	@Override
	public void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols) {
		SortedMap<String, Long> timestampedFiles = new TreeMap<>();

		timestampedFiles = Arrays.stream(files)
				.filter(file -> new File(file).exists())
				.collect(Collectors.toMap(file -> file, file -> new File(file).lastModified(), (v1,v2) -> { throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));

		save(cacheKey, generatedSymbols, timestampedFiles);
	}

	@Override
	public CachedSymbol[] retrieve(SymbolCacheKey cacheKey, String[] files) {
		try {
			File cacheStore = new File(cacheDirectory, cacheKey.toString() + ".json");
			if (cacheStore.exists()) {
				Gson gson = createGson();
				JsonReader reader = new JsonReader(new FileReader(cacheStore));
				CacheStore store = gson.fromJson(reader, CacheStore.class);

				SortedMap<String, Long> timestampedFiles = Arrays.stream(files)
						.filter(file -> new File(file).exists())
						.collect(Collectors.toMap(file -> file, file -> new File(file).lastModified(),  (v1,v2) -> { throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));

				if (isFileMatch(timestampedFiles, store.getTimestampedFiles())) {
					this.stores.put(cacheKey, store);

					List<CachedSymbol> symbols = store.getSymbols();
					return (CachedSymbol[]) symbols.toArray(new CachedSymbol[symbols.size()]);
				}
			}
		}
		catch (Exception e) {
			log.error("error reading cached symbols", e);
		}
		return null;
	}

	@Override
	public void removeFile(SymbolCacheKey cacheKey, String file) {
		CacheStore cacheStore = this.stores.get(cacheKey);
		if (cacheStore != null) {
			String docURI = UriUtil.toUri(new File(file)).toString();

			SortedMap<String, Long> timestampedFiles = new TreeMap<>(cacheStore.getTimestampedFiles());
			timestampedFiles.remove(file);

			List<CachedSymbol> cachedSymbols = cacheStore.getSymbols().stream()
					.filter(cachedSymbol -> !cachedSymbol.getDocURI().equals(docURI))
					.collect(Collectors.toList());

			save(cacheKey, cachedSymbols, timestampedFiles);
		}
	}

	@Override
	public void remove(SymbolCacheKey cacheKey) {
		File cacheStore = new File(cacheDirectory, cacheKey.toString() + ".json");
		if (cacheStore.exists()) {
			cacheStore.delete();
			this.stores.remove(cacheKey);
		}
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols) {
		CacheStore cacheStore = this.stores.get(cacheKey);

		if (cacheStore != null) {
			String docURI = UriUtil.toUri(new File(file)).toString();

			SortedMap<String, Long> timestampedFiles = new TreeMap<>(cacheStore.getTimestampedFiles());
			timestampedFiles.put(file, lastModified);

			List<CachedSymbol> cachedSymbols = cacheStore.getSymbols().stream()
					.filter(cachedSymbol -> !cachedSymbol.getDocURI().equals(docURI))
					.collect(Collectors.toList());

			cachedSymbols.addAll(generatedSymbols);

			save(cacheKey, cachedSymbols, timestampedFiles);
		}
	}

	private void save(SymbolCacheKey cacheKey, List<CachedSymbol> generatedSymbols,
			SortedMap<String, Long> timestampedFiles) {
		CacheStore store = new CacheStore(cacheKey.toString(), timestampedFiles, generatedSymbols);
		this.stores.put(cacheKey, store);

		try (FileWriter writer = new FileWriter(new File(cacheDirectory, cacheKey.toString() + ".json")))
		{
			Gson gson = createGson();
			gson.toJson(store, writer);

			cleanupCacheFiles(cacheKey);
		}
		catch (Exception e) {
			log.error("cannot write symbol cache", e);
		}
	}

	private boolean isFileMatch(SortedMap<String, Long> files1, SortedMap<String, Long> files2) {
		if (files1.size() != files2.size()) return false;

		for (String file : files1.keySet()) {
			if (!files2.containsKey(file)) return false;
			if (!files1.get(file).equals(files2.get(file))) return false;
		}

		return true;
	}

	private void cleanupCacheFiles(SymbolCacheKey cacheKey) {
		File[] cacheFiles = this.cacheDirectory.listFiles();

		for (int i = 0; i < cacheFiles.length; i++) {
			String fileName = cacheFiles[i].getName();
			SymbolCacheKey key = SymbolCacheKey.parse(fileName);

			if (key != null && !key.equals(cacheKey)
					&& key.getPrimaryIdentifier().equals(cacheKey.getPrimaryIdentifier())) {
				cacheFiles[i].delete();
			}
		}
	}

	private Gson createGson() {
		return new GsonBuilder().registerTypeAdapter(SymbolAddOnInformation.class, new SymbolAddOnInformationAdapter()).create();
	}


	/**
	 * internal storage structure
	 */
	private static class CacheStore {

		private final String cacheKey;
		private final SortedMap<String, Long> timestampedFiles;
		private final List<CachedSymbol> symbols;

		public CacheStore(String cacheKey, SortedMap<String, Long> timestampedFiles, List<CachedSymbol> symbols) {
			super();
			this.cacheKey = cacheKey;
			this.timestampedFiles = timestampedFiles;
			this.symbols = symbols;
		}

		public String getCacheKey() {
			return cacheKey;
		}

		public List<CachedSymbol> getSymbols() {
			return symbols;
		}

		public SortedMap<String, Long> getTimestampedFiles() {
			return timestampedFiles;
		}

	}

	/**
	 * gson adapter to store subtype information for symbol addon informations
	 */
	private static class SymbolAddOnInformationAdapter implements JsonSerializer<SymbolAddOnInformation>, JsonDeserializer<SymbolAddOnInformation> {

	    @Override
	    public JsonElement serialize(SymbolAddOnInformation addonInfo, Type typeOfSrc, JsonSerializationContext context) {
	        JsonObject result = new JsonObject();
	        result.add("type", new JsonPrimitive(addonInfo.getClass().getName()));
	        result.add("data", context.serialize(addonInfo));
	        return result;
	    }

	    @Override
	    public SymbolAddOnInformation deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
	        JsonObject parsedObject = json.getAsJsonObject();
	        String className = parsedObject.get("type").getAsString();
	        JsonElement element = parsedObject.get("data");

	        try {
	            return context.deserialize(element, Class.forName(className));
	        } catch (ClassNotFoundException cnfe) {
	            throw new JsonParseException("cannot parse data from unknown SymbolAddOnInformation subtype: " + type, cnfe);
	        }
	    }
	}


}
