package org.springframework.ide.vscode.boot.properties.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;

public class PropertiesLoader {
	
	private static final String MAIN_SPRING_CONFIGURATION_METADATA_JSON = "META-INF/spring-configuration-metadata.json";

	public static final String ADDITIONAL_SPRING_CONFIGURATION_METADATA_JSON = "META-INF/additional-spring-configuration-metadata.json";
	
	/**
	 * The default classpath location for config metadata loaded when scanning .jar files on the classpath.
	 */
	public static final String[] JAR_META_DATA_LOCATIONS = {
		MAIN_SPRING_CONFIGURATION_METADATA_JSON
		//Not scanning 'additional' metadata because it integrated already in the main data.
	};

	/**
	 * The default classpath location for config metadata loaded when scanning project output folders.
	 */
	public static final String[] PROJECT_META_DATA_LOCATIONS = {
		MAIN_SPRING_CONFIGURATION_METADATA_JSON,
		ADDITIONAL_SPRING_CONFIGURATION_METADATA_JSON
	};
	
    private static final Logger LOG = Logger.getLogger(PropertiesLoader.class.getName());
    
	private ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
	
	public ConfigurationMetadataRepository load(Path projectPath) {
		return load(projectPath.resolve("classpath.txt"), projectPath.resolve("target/classes"));
	}
	
	private ConfigurationMetadataRepository load(Path classPathFilePath, Path outputFolderPath) {
		loadFromClasspath(classPathFilePath);
		loadFromOutputFolder(outputFolderPath);
		ConfigurationMetadataRepository repository = builder.build();
		return repository;
	}
	
    private void loadFromClasspath(Path classPathFilePath) {
    	if (classPathFilePath != null && Files.exists(classPathFilePath)) {
    		readClassPathFile(classPathFilePath).ifPresent(classPathSet -> {
    			classPathSet.stream().forEach(this::loadFromJar);
    		});
    	}
    }

    private Optional<Set<Path>> readClassPathFile(Path classPathFilePath){
    	try {
			InputStream in = Files.newInputStream(classPathFilePath);
			String text = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining());
			Path dir = classPathFilePath.getParent();
			return Optional.of(Arrays.stream(text.split(File.pathSeparator)).map(dir::resolve).collect(Collectors.toSet()));
    	} catch (IOException e) {
    		LOG.log(Level.SEVERE, "Failed to read classpath text file", e);
    		return Optional.empty();
    	}
    }

	private void loadFromOutputFolder(Path outputFolderPath) {
		if (outputFolderPath != null && Files.exists(outputFolderPath)) {
			Arrays.stream(PROJECT_META_DATA_LOCATIONS).forEach(mdLoc -> {
				loadFromJsonFile(outputFolderPath.resolve(mdLoc));
			});
		}
	}

	private void loadFromJsonFile(Path mdf) {
		if (Files.exists(mdf)) {
			InputStream is = null;
			try {
				is = Files.newInputStream(mdf);
				loadFromInputStream(mdf, is);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Error loading file '" + mdf + "'", e);
			} finally {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException e) {
						//ignore
					}
				}
			}
		}
	}
	
	private void loadFromJar(Path f) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(f.toFile());
			//jarDump(jarFile);
			for (String loc : JAR_META_DATA_LOCATIONS) {
				ZipEntry e = jarFile.getEntry(loc);
				if (e!=null) {
					loadFrom(jarFile, e);
				}
			}
		} catch (Throwable e) {
			LOG.log(Level.SEVERE, "Error loading JAR file", e);
		} finally {
			if (jarFile!=null) {
				try {
					jarFile.close();
				} catch (IOException e) {
				}
			}
		}
	}


	private void loadFrom(JarFile jarFile, ZipEntry ze) {
		InputStream is = null;
		try {
			is = jarFile.getInputStream(ze);
			loadFromInputStream(jarFile.getName()+"["+ze.getName()+"]", is);
		} catch (Throwable e) {
			LOG.log(Level.SEVERE, "Error loading JAR file", e);
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void loadFromInputStream(Object origin, InputStream is) throws IOException {
		builder.withJsonResource(origin, is);
	}
	
	public static void main(String[] args) {
		if (args.length > 0) {
			Path projectPath = Paths.get(args[0]);			
			ConfigurationMetadataRepository repo = new PropertiesLoader().load(projectPath);
			Map<String, ConfigurationMetadataProperty> allProperties = repo.getAllProperties();
			allProperties.keySet().forEach(System.out::println);
		}
	}

}
