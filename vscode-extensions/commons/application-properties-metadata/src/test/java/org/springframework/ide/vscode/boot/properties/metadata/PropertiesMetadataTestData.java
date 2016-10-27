/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.metadata;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.boot.configurationmetadata.ValueProvider;
import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndex;
import org.springframework.ide.vscode.application.properties.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.commons.java.IClasspath;

/**
 * Boot properties Metadata generated artificially for the tests 
 * 
 * @author Alex Boyko
 *
 */
public class PropertiesMetadataTestData {

	public class ItemConfigurer {

		private ConfigurationMetadataProperty item;

		public ItemConfigurer(ConfigurationMetadataProperty item) {
			this.item = item;
		}

		/**
		 * Add a provider with a single parameter.
		 * @return
		 */
		public ItemConfigurer provider(String name, String paramName, Object paramValue) {
			ValueProvider provider = new ValueProvider();
			provider.setName(name);
			provider.getParameters().put(paramName, paramValue);
			item.getHints().getValueProviders().add(provider);
			return this;
		}

		/**
		 * Add a value hint. If description contains a '.' the dot is used
		 * to break description into a short and long description.
		 * @return
		 */
		public ItemConfigurer valueHint(Object value, String description) {
			ValueHint hint = new ValueHint();
			hint.setValue(value);
			if (description!=null) {
				int dotPos = description.indexOf('.');
				if (dotPos>=0) {
					hint.setShortDescription( description.substring(0, dotPos));
				}
				hint.setDescription(description);
			}
			item.getHints().getValueHints().add(hint);
			return this;
		}
	}

	Map<String, ConfigurationMetadataProperty> datas = new LinkedHashMap<>();

	public ItemConfigurer addPropertyMetadata(String id, String type, Object deflt, String description,
			String... source
	) {
		ConfigurationMetadataProperty item = new ConfigurationMetadataProperty();
		item.setId(id);
		item.setDescription(description);
		item.setType(type);
		item.setDefaultValue(deflt);
		datas.put(item.getId(), item);
		return new ItemConfigurer(item);
	}
	
	public SpringPropertyIndex createIndex() {
		SpringPropertyIndex index =	new SpringPropertyIndex(ValueProviderRegistry.getDefault(), new IClasspath() {
			@Override
			public Collection<Path> getClasspathEntries() throws Exception {
				return Collections.emptyList();
			}
		});
		for (ConfigurationMetadataProperty propertyInfo : datas.values()) {
			index.add(propertyInfo);
		}
		return index;
	}

	/**
	 * Call this method to add some default test data to the Completion engine's index.
	 * Note that this data is not added automatically, some test may want to use smaller
	 * test data sets.
	 */
	public void defaultTestData() {
		addPropertyMetadata("banner.charset", "java.nio.charset.Charset", "UTF-8", "Banner file encoding.");
		addPropertyMetadata("banner.location", "java.lang.String", "classpath:banner.txt", "Banner file location.");
		addPropertyMetadata("debug", "java.lang.Boolean", "false", "Enable debug logs.");
		addPropertyMetadata("flyway.check-location", "java.lang.Boolean", "false", "Check that migration scripts location exists.");
		addPropertyMetadata("flyway.clean-on-validation-error", "java.lang.Boolean", null, null);
		addPropertyMetadata("flyway.enabled", "java.lang.Boolean", "true", "Enable flyway.");
		addPropertyMetadata("flyway.encoding", "java.lang.String", null, null);
		addPropertyMetadata("flyway.ignore-failed-future-migration", "java.lang.Boolean", null, null);
		addPropertyMetadata("flyway.init-description", "java.lang.String", null, null);
		addPropertyMetadata("flyway.init-on-migrate", "java.lang.Boolean", null, null);
		addPropertyMetadata("flyway.init-sqls", "java.util.List<java.lang.String>", null, "SQL statements to execute to initialize a connection immediately after obtaining\n it.");
		addPropertyMetadata("flyway.init-version", "org.flywaydb.core.api.MigrationVersion", null, null);
		addPropertyMetadata("flyway.locations", "java.util.List<java.lang.String>", null, "Locations of migrations scripts.");
		addPropertyMetadata("flyway.out-of-order", "java.lang.Boolean", null, null);
		addPropertyMetadata("flyway.password", "java.lang.String", null, "Login password of the database to migrate.");
		addPropertyMetadata("flyway.placeholder-prefix", "java.lang.String", null, null);
		addPropertyMetadata("flyway.placeholders", "java.util.Map<java.lang.String,java.lang.String>", null, null);
		addPropertyMetadata("flyway.placeholder-suffix", "java.lang.String", null, null);
		addPropertyMetadata("flyway.schemas", "java.lang.String[]", null, null);
		addPropertyMetadata("flyway.sql-migration-prefix", "java.lang.String", null, null);
		addPropertyMetadata("flyway.sql-migration-separator", "java.lang.String", null, null);
		addPropertyMetadata("flyway.sql-migration-suffix", "java.lang.String", null, null);
		addPropertyMetadata("flyway.table", "java.lang.String", null, null);
		addPropertyMetadata("flyway.target", "org.flywaydb.core.api.MigrationVersion", null, null);
		addPropertyMetadata("flyway.url", "java.lang.String", null, "JDBC url of the database to migrate. If not set, the primary configured data source\n is used.");
		addPropertyMetadata("flyway.user", "java.lang.String", null, "Login user of the database to migrate.");
		addPropertyMetadata("flyway.validate-on-migrate", "java.lang.Boolean", null, null);
		addPropertyMetadata("http.mappers.json-pretty-print", "java.lang.Boolean", null, "Enable json pretty print.");
		addPropertyMetadata("http.mappers.json-sort-keys", "java.lang.Boolean", null, "Enable key sorting.");
		addPropertyMetadata("liquibase.change-log", "java.lang.String", "classpath:/db/changelog/db.changelog-master.yaml", "Change log configuration path.");
		addPropertyMetadata("liquibase.check-change-log-location", "java.lang.Boolean", "true", "Check the change log location exists.");
		addPropertyMetadata("liquibase.contexts", "java.lang.String", null, "Comma-separated list of runtime contexts to use.");
		addPropertyMetadata("liquibase.default-schema", "java.lang.String", null, "Default database schema.");
		addPropertyMetadata("liquibase.drop-first", "java.lang.Boolean", "false", "Drop the database schema first.");
		addPropertyMetadata("liquibase.enabled", "java.lang.Boolean", "true", "Enable liquibase support.");
		addPropertyMetadata("liquibase.password", "java.lang.String", null, "Login password of the database to migrate.");
		addPropertyMetadata("liquibase.url", "java.lang.String", null, "JDBC url of the database to migrate. If not set, the primary configured data source\n is used.");
		addPropertyMetadata("liquibase.user", "java.lang.String", null, "Login user of the database to migrate.");
		addPropertyMetadata("logging.config", "java.lang.String", null, "Location of the logging configuration file.");
		addPropertyMetadata("logging.file", "java.lang.String", null, "Log file name.");
		addPropertyMetadata("logging.level", "java.util.Map<java.lang.String,java.lang.Object>", null, "Log levels severity mapping. Use 'root' for the root logger.");
		addPropertyMetadata("logging.path", "java.lang.String", null, "Location of the log file.");
		addPropertyMetadata("multipart.file-size-threshold", "java.lang.String", "0", "Threshold after which files will be written to disk. Values can use the suffixed\n \"MB\" or \"KB\" to indicate a Megabyte or Kilobyte size.");
		addPropertyMetadata("multipart.location", "java.lang.String", null, "Intermediate location of uploaded files.");
		addPropertyMetadata("multipart.max-file-size", "java.lang.String", "1Mb", "Max file size. Values can use the suffixed \"MB\" or \"KB\" to indicate a Megabyte or\n Kilobyte size.");
		addPropertyMetadata("multipart.max-request-size", "java.lang.String", "10Mb", "Max request size. Values can use the suffixed \"MB\" or \"KB\" to indicate a Megabyte\n or Kilobyte size.");
		addPropertyMetadata("security.basic.enabled", "java.lang.Boolean", "true", "Enable basic authentication.");
		addPropertyMetadata("security.basic.path", "java.lang.String[]", "[Ljava.lang.Object;@7abd0056", "Comma-separated list of paths to secure.");
		addPropertyMetadata("security.basic.realm", "java.lang.String", "Spring", "HTTP basic realm name.");
		addPropertyMetadata("security.enable-csrf", "java.lang.Boolean", "false", "Enable Cross Site Request Forgery support.");
		addPropertyMetadata("security.filter-order", "java.lang.Integer", "0", "Security filter chain order.");
		addPropertyMetadata("security.headers.cache", "java.lang.Boolean", "false", "Enable cache control HTTP headers.");
		addPropertyMetadata("security.headers.content-type", "java.lang.Boolean", "false", "Enable \"X-Content-Type-Options\" header.");
		addPropertyMetadata("security.headers.frame", "java.lang.Boolean", "false", "Enable \"X-Frame-Options\" header.");
		addPropertyMetadata("security.headers.hsts", "org.springframework.boot.autoconfigure.security.SecurityProperties$Headers$HSTS", null, "HTTP Strict Transport Security (HSTS) mode (none, domain, all).");
		addPropertyMetadata("security.headers.xss", "java.lang.Boolean", "false", "Enable cross site scripting (XSS) protection.");
		addPropertyMetadata("security.ignored", "java.util.List<java.lang.String>", null, "Comma-separated list of paths to exclude from the default secured paths.");
		addPropertyMetadata("security.require-ssl", "java.lang.Boolean", "false", "Enable secure channel for all requests.");
		addPropertyMetadata("security.sessions", "org.springframework.security.config.http.SessionCreationPolicy", null, "Session creation policy (always, never, if_required, stateless).");
		addPropertyMetadata("security.user.name", "java.lang.String", "user", "Default user name.");
		addPropertyMetadata("security.user.password", "java.lang.String", null, "Password for the default user name.");
		addPropertyMetadata("security.user.role", "java.util.List<java.lang.String>", null, "Granted roles for the default user name.");
		addPropertyMetadata("server.address", "java.net.InetAddress", null, "Network address to which the server should bind to.");
		addPropertyMetadata("server.context-parameters", "java.util.Map<java.lang.String,java.lang.String>", null, "ServletContext parameters.");
		addPropertyMetadata("server.context-path", "java.lang.String", null, "Context path of the application.");
		addPropertyMetadata("server.port", "java.lang.Integer", null, "Server HTTP port.");
		addPropertyMetadata("server.servlet-path", "java.lang.String", "/", "Path of the main dispatcher servlet.");
		addPropertyMetadata("server.session-timeout", "java.lang.Integer", null, "Session timeout in seconds.");
		addPropertyMetadata("server.ssl.ciphers", "java.lang.String[]", null, null);
		addPropertyMetadata("server.ssl.client-auth", "org.springframework.boot.context.embedded.Ssl$ClientAuth", null, null);
		addPropertyMetadata("server.ssl.key-alias", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.key-password", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.key-store", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.key-store-password", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.key-store-provider", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.key-store-type", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.protocol", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.trust-store", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.trust-store-password", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.trust-store-provider", "java.lang.String", null, null);
		addPropertyMetadata("server.ssl.trust-store-type", "java.lang.String", null, null);
		addPropertyMetadata("server.tomcat.access-log-enabled", "java.lang.Boolean", "false", "Enable access log.");
		addPropertyMetadata("server.tomcat.access-log-pattern", "java.lang.String", null, "Format pattern for access logs.");
		addPropertyMetadata("server.tomcat.background-processor-delay", "java.lang.Integer", "30", "Delay in seconds between the invocation of backgroundProcess methods.");
		addPropertyMetadata("server.tomcat.basedir", "java.io.File", null, "Tomcat base directory. If not specified a temporary directory will be used.");
		addPropertyMetadata("server.tomcat.internal-proxies", "java.lang.String", "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|169\\.254\\.\\d{1,3}\\.\\d{1,3}|127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "Regular expression that matches proxies that are to be trusted.");
		addPropertyMetadata("server.tomcat.max-http-header-size", "java.lang.Integer", "0", "Maximum size in bytes of the HTTP message header.");
		addPropertyMetadata("server.tomcat.max-threads", "java.lang.Integer", "0", "Maximum amount of worker threads.");
		addPropertyMetadata("server.tomcat.port-header", "java.lang.String", null, "Name of the HTTP header used to override the original port value.");
		addPropertyMetadata("server.tomcat.protocol-header", "java.lang.String", null, "Header that holds the incoming protocol, usually named \"X-Forwarded-Proto\".\n Configured as a RemoteIpValve only if remoteIpHeader is also set.");
		addPropertyMetadata("server.tomcat.remote-ip-header", "java.lang.String", null, "Name of the http header from which the remote ip is extracted. Configured as a\n RemoteIpValve only if remoteIpHeader is also set.");
		addPropertyMetadata("server.tomcat.uri-encoding", "java.lang.String", null, "Character encoding to use to decode the URI.");
		addPropertyMetadata("server.undertow.buffer-size", "java.lang.Integer", null, "Size of each buffer in bytes.");
		addPropertyMetadata("server.undertow.buffers-per-region", "java.lang.Integer", null, "Number of buffer per region.");
		addPropertyMetadata("server.undertow.direct-buffers", "java.lang.Boolean", null, null);
		addPropertyMetadata("server.undertow.io-threads", "java.lang.Integer", null, "Number of I/O threads to create for the worker.");
		addPropertyMetadata("server.undertow.worker-threads", "java.lang.Integer", null, "Number of worker threads.");
		addPropertyMetadata("spring.activemq.broker-url", "java.lang.String", null, "URL of the ActiveMQ broker. Auto-generated by default.");
		addPropertyMetadata("spring.activemq.in-memory", "java.lang.Boolean", "true", "Specify if the default broker URL should be in memory. Ignored if an explicit\n broker has been specified.");
		addPropertyMetadata("spring.activemq.password", "java.lang.String", null, "Login password of the broker.");
		addPropertyMetadata("spring.activemq.pooled", "java.lang.Boolean", "false", "Specify if a PooledConnectionFactory should be created instead of a regular\n ConnectionFactory.");
		addPropertyMetadata("spring.activemq.user", "java.lang.String", null, "Login user of the broker.");
		addPropertyMetadata("spring.aop.auto", "java.lang.Boolean", "true", "Add @EnableAspectJAutoProxy.");
		addPropertyMetadata("spring.aop.proxy-target-class", "java.lang.Boolean", "false", "Whether subclass-based (CGLIB) proxies are to be created (true) as opposed to standard Java interface-based proxies (false).");
		addPropertyMetadata("spring.application.index", "java.lang.Integer", null, "Application index.");
		addPropertyMetadata("spring.application.name", "java.lang.String", null, "Application name.");
		addPropertyMetadata("spring.batch.initializer.enabled", "java.lang.Boolean", "true", "Create the required batch tables on startup if necessary.");
		addPropertyMetadata("spring.batch.job.enabled", "java.lang.Boolean", "true", "Execute all Spring Batch jobs in the context on startup.");
		addPropertyMetadata("spring.batch.job.names", "java.lang.String", "", "Comma-separated list of job names to execute on startup. By default, all Jobs\n found in the context are executed.");
		addPropertyMetadata("spring.batch.schema", "java.lang.String", "classpath:org/springframework/batch/core/schema-@@platform@@.sql", "Path to the SQL file to use to initialize the database schema.");
		addPropertyMetadata("spring.config.location", "java.lang.String", null, "Config file locations.");
		addPropertyMetadata("spring.config.name", "java.lang.String", "application", "Config file name.");
		addPropertyMetadata("spring.dao.exceptiontranslation.enabled", "java.lang.Boolean", "true", "Enable the PersistenceExceptionTranslationPostProcessor.");
		addPropertyMetadata("spring.data.elasticsearch.cluster-name", "java.lang.String", "elasticsearch", "Elasticsearch cluster name.");
		addPropertyMetadata("spring.data.elasticsearch.cluster-nodes", "java.lang.String", null, "Comma-separated list of cluster node addresses. If not specified, starts a client\n node.");
		addPropertyMetadata("spring.data.elasticsearch.repositories.enabled", "java.lang.Boolean", "true", "Enable Elasticsearch repositories.");
		addPropertyMetadata("spring.data.jpa.repositories.enabled", "java.lang.Boolean", "true", "Enable JPA repositories.");
		addPropertyMetadata("spring.data.mongodb.authentication-database", "java.lang.String", null, "Authentication database name.");
		addPropertyMetadata("spring.data.mongodb.database", "java.lang.String", null, "Database name.");
		addPropertyMetadata("spring.data.mongodb.grid-fs-database", "java.lang.String", null, "GridFS database name.");
		addPropertyMetadata("spring.data.mongodb.host", "java.lang.String", null, "Mongo server host.");
		addPropertyMetadata("spring.data.mongodb.password", "char[]", null, "Login password of the mongo server.");
		addPropertyMetadata("spring.data.mongodb.port", "java.lang.Integer", null, "Mongo server port.");
		addPropertyMetadata("spring.data.mongodb.repositories.enabled", "java.lang.Boolean", "true", "Enable Mongo repositories.");
		addPropertyMetadata("spring.data.mongodb.uri", "java.lang.String", "mongodb://localhost/test", "Mmongo database URI. When set, host and port are ignored.");
		addPropertyMetadata("spring.data.mongodb.username", "java.lang.String", null, "Login user of the mongo server.");
		addPropertyMetadata("spring.data.rest.base-uri", "java.net.URI", null, null);
		addPropertyMetadata("spring.data.rest.default-page-size", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.data.rest.limit-param-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.data.rest.max-page-size", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.data.rest.page-param-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.data.rest.return-body-on-create", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.data.rest.return-body-on-update", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.data.rest.sort-param-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.data.solr.host", "java.lang.String", "http://127.0.0.1:8983/solr", "Solr host. Ignored if \"zk-host\" is set.");
		addPropertyMetadata("spring.data.solr.repositories.enabled", "java.lang.Boolean", "true", "Enable Solr repositories.");
		addPropertyMetadata("spring.data.solr.zk-host", "java.lang.String", null, "ZooKeeper host address in the form HOST:PORT.");
		addPropertyMetadata("spring.datasource.abandon-when-percentage-full", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.access-to-underlying-connection-allowed", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.alternate-username-allowed", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.auto-commit", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.catalog", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.commit-on-return", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.connection-customizer-class-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.connection-init-sql", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.connection-init-sqls", "java.util.Collection", null, null);
		addPropertyMetadata("spring.datasource.connection-properties", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.connection-test-query", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.connection-timeout", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.continue-on-error", "java.lang.Boolean", "false", "Do not stop if an error occurs while initializing the database.");
		addPropertyMetadata("spring.datasource.data", "java.lang.String", null, "Data (DML) script resource reference.");
		addPropertyMetadata("spring.datasource.data-source-class-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.data-source", "java.lang.Object", null, null);
		addPropertyMetadata("spring.datasource.data-source-j-n-d-i", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.data-source-properties", "java.util.Properties", null, null);
		addPropertyMetadata("spring.datasource.db-properties", "java.util.Properties", null, null);
		addPropertyMetadata("spring.datasource.default-auto-commit", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.default-catalog", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.default-read-only", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.default-transaction-isolation", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.driver-class-name", "java.lang.String", null, "Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.");
		addPropertyMetadata("spring.datasource.fair-queue", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.idle-timeout", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.ignore-exception-on-pre-load", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.initialization-fail-fast", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.initialize", "java.lang.Boolean", "true", "Populate the database using 'data.sql'.");
		addPropertyMetadata("spring.datasource.initial-size", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.init-s-q-l", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.isolate-internal-queries", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.jdbc4-connection-test", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.jdbc-interceptors", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.jdbc-url", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.jmx-enabled", "java.lang.Boolean", "false", "Enable JMX support (if provided by the underlying pool).");
		addPropertyMetadata("spring.datasource.jndi-name", "java.lang.String", null, "JNDI location of the datasource. Class, url, username & password are ignored when\n set.");
		addPropertyMetadata("spring.datasource.leak-detection-threshold", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.log-abandoned", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.login-timeout", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.log-validation-errors", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.max-active", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.max-age", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.max-idle", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.maximum-pool-size", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.max-lifetime", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.max-open-prepared-statements", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.max-wait", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.metric-registry", "java.lang.Object", null, null);
		addPropertyMetadata("spring.datasource.min-evictable-idle-time-millis", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.min-idle", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.minimum-idle", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.name", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.num-tests-per-eviction-run", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.password", "java.lang.String", null, "Login password of the database.");
		addPropertyMetadata("spring.datasource.platform", "java.lang.String", "all", "Platform to use in the schema resource (schema-${platform}.sql).");
		addPropertyMetadata("spring.datasource.pool-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.pool-prepared-statements", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.propagate-interrupt-state", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.read-only", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.register-mbeans", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.remove-abandoned", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.remove-abandoned-timeout", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.rollback-on-return", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.schema", "java.lang.String", null, "Schema (DDL) script resource reference.");
		addPropertyMetadata("spring.datasource.separator", "java.lang.String", ";", "Statement separator in SQL initialization scripts.");
		addPropertyMetadata("spring.datasource.sql-script-encoding", "java.lang.String", null, "SQL scripts encoding.");
		addPropertyMetadata("spring.datasource.suspect-timeout", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.test-on-borrow", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.test-on-connect", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.test-on-return", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.test-while-idle", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.time-between-eviction-runs-millis", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.transaction-isolation", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.url", "java.lang.String", null, "JDBC url of the database.");
		addPropertyMetadata("spring.datasource.use-disposable-connection-facade", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.use-equals", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.use-lock", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.datasource.username", "java.lang.String", null, "Login user of the database.");
		addPropertyMetadata("spring.datasource.validation-interval", "java.lang.Long", null, null);
		addPropertyMetadata("spring.datasource.validation-query", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.validation-query-timeout", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.datasource.validator-class-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.datasource.xa.data-source-class-name", "java.lang.String", null, "XA datasource fully qualified name.");
		addPropertyMetadata("spring.datasource.xa.properties", "java.util.Map<java.lang.String,java.lang.String>", null, "Properties to pass to the XA data source.");
		addPropertyMetadata("spring.freemarker.allow-request-override", "java.lang.Boolean", null, "Set whether HttpServletRequest attributes are allowed to override (hide) controller\n generated model attributes of the same name.");
		addPropertyMetadata("spring.freemarker.cache", "java.lang.Boolean", null, "Enable template caching.");
		addPropertyMetadata("spring.freemarker.char-set", "java.lang.String", null, null);
		addPropertyMetadata("spring.freemarker.charset", "java.lang.String", null, "Template encoding.");
		addPropertyMetadata("spring.freemarker.check-template-location", "java.lang.Boolean", null, "Check that the templates location exists.");
		addPropertyMetadata("spring.freemarker.content-type", "java.lang.String", null, "Content-Type value.");
		addPropertyMetadata("spring.freemarker.enabled", "java.lang.Boolean", null, "Enable MVC view resolution for this technology.");
		addPropertyMetadata("spring.freemarker.expose-request-attributes", "java.lang.Boolean", null, "Set whether all request attributes should be added to the model prior to merging\n with the template.");
		addPropertyMetadata("spring.freemarker.expose-session-attributes", "java.lang.Boolean", null, "Set whether all HttpSession attributes should be added to the model prior to\n merging with the template.");
		addPropertyMetadata("spring.freemarker.expose-spring-macro-helpers", "java.lang.Boolean", null, "Set whether to expose a RequestContext for use by Spring's macro library, under the\n name \"springMacroRequestContext\".");
		addPropertyMetadata("spring.freemarker.prefix", "java.lang.String", null, "Prefix that gets prepended to view names when building a URL.");
		addPropertyMetadata("spring.freemarker.request-context-attribute", "java.lang.String", null, "Name of the RequestContext attribute for all views.");
		addPropertyMetadata("spring.freemarker.settings", "java.util.Map<java.lang.String,java.lang.String>", null, "Well-known FreeMarker keys which will be passed to FreeMarker's Configuration.");
		addPropertyMetadata("spring.freemarker.suffix", "java.lang.String", null, "Suffix that gets appended to view names when building a URL.");
		addPropertyMetadata("spring.freemarker.template-loader-path", "java.lang.String[]", new String[] {"snuzzle" ,"buggles"}, "Comma-separated list of template paths.");
		addPropertyMetadata("spring.freemarker.view-names", "java.lang.String[]", null, "White list of view names that can be resolved.");
		addPropertyMetadata("spring.groovy.template.cache", "java.lang.Boolean", null, "Enable template caching.");
		addPropertyMetadata("spring.groovy.template.char-set", "java.lang.String", null, null);
		addPropertyMetadata("spring.groovy.template.charset", "java.lang.String", null, "Template encoding.");
		addPropertyMetadata("spring.groovy.template.check-template-location", "java.lang.Boolean", null, "Check that the templates location exists.");
		addPropertyMetadata("spring.groovy.template.configuration.auto-escape", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.auto-indent", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.auto-indent-string", "java.lang.String", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.auto-new-line", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.base-template-class", "java.lang.Class<? extends groovy.text.markup.BaseTemplate>", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.cache-templates", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.declaration-encoding", "java.lang.String", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.expand-empty-elements", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.configuration", "java.util.Map<java.lang.String,java.lang.Object>", null, "Configuration to pass to TemplateConfiguration.");
		addPropertyMetadata("spring.groovy.template.configuration.locale", "java.util.Locale", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.new-line-string", "java.lang.String", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.resource-loader-path", "java.lang.String", null, null);
		addPropertyMetadata("spring.groovy.template.configuration.use-double-quotes", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.groovy.template.content-type", "java.lang.String", null, "Content-Type value.");
		addPropertyMetadata("spring.groovy.template.enabled", "java.lang.Boolean", null, "Enable MVC view resolution for this technology.");
		addPropertyMetadata("spring.groovy.template.prefix", "java.lang.String", "classpath:/templates/", "Prefix that gets prepended to view names when building a URL.");
		addPropertyMetadata("spring.groovy.template.suffix", "java.lang.String", ".tpl", "Suffix that gets appended to view names when building a URL.");
		addPropertyMetadata("spring.groovy.template.view-names", "java.lang.String[]", null, "White list of view names that can be resolved.");
		addPropertyMetadata("spring.hornetq.embedded.cluster-password", "java.lang.String", null, "Cluster password. Randomly generated on startup by default");
		addPropertyMetadata("spring.hornetq.embedded.data-directory", "java.lang.String", null, "Journal file directory. Not necessary if persistence is turned off.");
		addPropertyMetadata("spring.hornetq.embedded.enabled", "java.lang.Boolean", "true", "Enable embedded mode if the HornetQ server APIs are available.");
		addPropertyMetadata("spring.hornetq.embedded.persistent", "java.lang.Boolean", "false", "Enable persistent store.");
		addPropertyMetadata("spring.hornetq.embedded.queues", "java.lang.String[]", "[Ljava.lang.Object;@2f5ce114", "Comma-separate list of queues to create on startup.");
		addPropertyMetadata("spring.hornetq.embedded.server-id", "java.lang.Integer", "0", "Server id. By default, an auto-incremented counter is used.");
		addPropertyMetadata("spring.hornetq.embedded.topics", "java.lang.String[]", "[Ljava.lang.Object;@6272137a", "Comma-separate list of topics to create on startup.");
		addPropertyMetadata("spring.hornetq.host", "java.lang.String", "localhost", "HornetQ broker host.");
		addPropertyMetadata("spring.hornetq.mode", "org.springframework.boot.autoconfigure.jms.hornetq.HornetQMode", null, "HornetQ deployment mode, auto-detected by default. Can be explicitly set to\n \"native\" or \"embedded\".");
		addPropertyMetadata("spring.hornetq.port", "java.lang.Integer", "5445", "HornetQ broker port.");
		addPropertyMetadata("spring.http.encoding.charset", "java.nio.charset.Charset", null, "Charset of HTTP requests and responses. Added to the \"Content-Type\" header if not\n set explicitly.");
		addPropertyMetadata("spring.http.encoding.enabled", "java.lang.Boolean", "true", "Enable http encoding support.");
		addPropertyMetadata("spring.http.encoding.force", "java.lang.Boolean", "true", "Force the encoding to the configured charset on HTTP requests and responses.");
		addPropertyMetadata("spring.jackson.date-format", "java.lang.String", null, "Date format string (yyyy-MM-dd HH:mm:ss), or a fully-qualified date format class\n name.");
		addPropertyMetadata("spring.jackson.deserialization", "java.util.Map<com.fasterxml.jackson.databind.DeserializationFeature,java.lang.Boolean>", null, "Jackson on/off features that affect the way Java objects are deserialized.");
		addPropertyMetadata("spring.jackson.generator", "java.util.Map<com.fasterxml.jackson.core.JsonGenerator.Feature,java.lang.Boolean>", null, "Jackson on/off features for generators.");
		addPropertyMetadata("spring.jackson.mapper", "java.util.Map<com.fasterxml.jackson.databind.MapperFeature,java.lang.Boolean>", null, "Jackson general purpose on/off features.");
		addPropertyMetadata("spring.jackson.parser", "java.util.Map<com.fasterxml.jackson.core.JsonParser.Feature,java.lang.Boolean>", null, "Jackson on/off features for parsers.");
		addPropertyMetadata("spring.jackson.property-naming-strategy", "java.lang.String", null, "One of the constants on Jackson's PropertyNamingStrategy\n (CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES). Can also be a fully-qualified class\n name of a PropertyNamingStrategy subclass.");
		addPropertyMetadata("spring.jackson.serialization", "java.util.Map<com.fasterxml.jackson.databind.SerializationFeature,java.lang.Boolean>", null, "Jackson on/off features that affect the way Java objects are serialized.");
		addPropertyMetadata("spring.jersey.filter.order", "java.lang.Integer", "0", "Jersey filter chain order.");
		addPropertyMetadata("spring.jersey.init", "java.util.Map<java.lang.String,java.lang.String>", null, "Init parameters to pass to Jersey.");
		addPropertyMetadata("spring.jersey.type", "org.springframework.boot.autoconfigure.jersey.JerseyProperties$Type", null, "Jersey integration type. Can be either \"servlet\" or \"filter\".");
		addPropertyMetadata("spring.jms.jndi-name", "java.lang.String", null, "Connection factory JNDI name. When set, takes precedence to others connection\n factory auto-configurations.");
		addPropertyMetadata("spring.jms.pub-sub-domain", "java.lang.Boolean", "false", "Specify if the default destination type is topic.");
		addPropertyMetadata("spring.jmx.enabled", "java.lang.Boolean", "true", "Expose management beans to the JMX domain.");
		addPropertyMetadata("spring.jpa.database", "org.springframework.orm.jpa.vendor.Database", null, "Target database to operate on, auto-detected by default. Can be alternatively set\n using the \"databasePlatform\" property.");
		addPropertyMetadata("spring.jpa.database-platform", "java.lang.String", null, "Name of the target database to operate on, auto-detected by default. Can be\n alternatively set using the \"Database\" enum.");
		addPropertyMetadata("spring.jpa.generate-ddl", "java.lang.Boolean", "false", "Initialize the schema on startup.");
		addPropertyMetadata("spring.jpa.hibernate.ddl-auto", "java.lang.String", null, "DDL mode (\"none\", \"validate\", \"update\", \"create\", \"create-drop\"). This is\n actually a shortcut for the \"hibernate.hbm2ddl.auto\" property. Default to\n \"create-drop\" when using an embedded database, \"none\" otherwise.");
		addPropertyMetadata("spring.jpa.hibernate.naming-strategy", "java.lang.Class<?>", null, "Naming strategy fully qualified name.");
		addPropertyMetadata("spring.jpa.open-in-view", "java.lang.Boolean", "true", "Register OpenEntityManagerInViewInterceptor. Binds a JPA EntityManager to the thread for the entire processing of the request.");
		addPropertyMetadata("spring.jpa.properties", "java.util.Map<java.lang.String,java.lang.String>", null, "Additional native properties to set on the JPA provider.");
		addPropertyMetadata("spring.jpa.show-sql", "java.lang.Boolean", "false", "Enable logging of SQL statements.");
		addPropertyMetadata("spring.jta.allow-multiple-lrc", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.asynchronous2-pc", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.background-recovery-interval", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.jta.background-recovery-interval-seconds", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.jta.current-node-only-recovery", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.debug-zero-resource-transaction", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.default-transaction-timeout", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.jta.disable-jmx", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.enabled", "java.lang.Boolean", "true", "Enable JTA support.");
		addPropertyMetadata("spring.jta.exception-analyzer", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.filter-log-status", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.force-batching-enabled", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.forced-write-enabled", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.graceful-shutdown-interval", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.jta.jndi-transaction-synchronization-registry-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.jndi-user-transaction-name", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.journal", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.log-dir", "java.lang.String", null, "Transaction logs directory.");
		addPropertyMetadata("spring.jta.log-part1-filename", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.log-part2-filename", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.max-log-size-in-mb", "java.lang.Integer", null, null);
		addPropertyMetadata("spring.jta.resource-configuration-filename", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.server-id", "java.lang.String", null, null);
		addPropertyMetadata("spring.jta.skip-corrupted-logs", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.jta.transaction-manager-id", "java.lang.String", null, "Transaction manager unique identifier.");
		addPropertyMetadata("spring.jta.warn-about-zero-resource-transaction", "java.lang.Boolean", null, null);
		addPropertyMetadata("spring.mail.default-encoding", "java.lang.String", "UTF-8", "Default MimeMessage encoding.");
		addPropertyMetadata("spring.mail.host", "java.lang.String", null, "SMTP server host.");
		addPropertyMetadata("spring.mail.password", "java.lang.String", null, "Login password of the SMTP server.");
		addPropertyMetadata("spring.mail.port", "java.lang.Integer", null, "SMTP server port.");
		addPropertyMetadata("spring.mail.properties", "java.util.Map<java.lang.String,java.lang.String>", null, "Additional JavaMail session properties.");
		addPropertyMetadata("spring.mail.username", "java.lang.String", null, "Login user of the SMTP server.");
		addPropertyMetadata("spring.main.show-banner", "java.lang.Boolean", "true", "Display the banner when the application runs.");
		addPropertyMetadata("spring.main.sources", "java.util.Set<java.lang.Object>", null, "Sources (class name, package name or XML resource location) used to create the ApplicationContext.");
		addPropertyMetadata("spring.main.web-environment", "java.lang.Boolean", null, "Run the application in a web environment (auto-detected by default).");
		addPropertyMetadata("spring.mandatory-file-encoding", "java.lang.String", null, "Expected character encoding the application must use.");
		addPropertyMetadata("spring.messages.basename", "java.lang.String", "messages", "Comma-separated list of basenames, each following the ResourceBundle convention.\n Essentially a fully-qualified classpath location. If it doesn't contain a package\n qualifier (such as \"org.mypackage\"), it will be resolved from the classpath root.");
		addPropertyMetadata("spring.messages.cache-seconds", "java.lang.Integer", "-1", "Loaded resource bundle files cache expiration, in seconds. When set to -1, bundles\n are cached forever.");
		addPropertyMetadata("spring.messages.encoding", "java.lang.String", "utf-8", "Message bundles encoding.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.enabled", "java.lang.Boolean", "false", "Enable device view resolver.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.mobile-prefix", "java.lang.String", "mobile/", "Prefix that gets prepended to view names for mobile devices.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.mobile-suffix", "java.lang.String", "", "Suffix that gets appended to view names for mobile devices.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.normal-prefix", "java.lang.String", "", "Prefix that gets prepended to view names for normal devices.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.normal-suffix", "java.lang.String", "", "Suffix that gets appended to view names for normal devices.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.tablet-prefix", "java.lang.String", "tablet/", "Prefix that gets prepended to view names for tablet devices.");
		addPropertyMetadata("spring.mobile.devicedelegatingviewresolver.tablet-suffix", "java.lang.String", "", "Suffix that gets appended to view names for tablet devices.");
		addPropertyMetadata("spring.mobile.sitepreference.enabled", "java.lang.Boolean", "true", "Enable SitePreferenceHandler.");
		addPropertyMetadata("spring.mvc.date-format", "java.lang.String", null, "Date format to use (e.g. dd/MM/yyyy)");
		addPropertyMetadata("spring.mvc.ignore-default-model-on-redirect", "java.lang.Boolean", "true", "If the the content of the \"default\" model should be ignored during redirect\n scenarios.");
		addPropertyMetadata("spring.mvc.locale", "java.lang.String", null, "Locale to use.");
		addPropertyMetadata("spring.mvc.message-codes-resolver-format", "org.springframework.validation.DefaultMessageCodesResolver$Format", null, "Formatting strategy for message codes (PREFIX_ERROR_CODE, POSTFIX_ERROR_CODE).");
		addPropertyMetadata("spring.profiles.active", "java.lang.String", null, "Comma-separated list of active profiles. Can be overridden by a command line switch.");
		addPropertyMetadata("spring.profiles.include", "java.lang.String", null, "Unconditionally activate the specified comma separated profiles.");
		addPropertyMetadata("spring.rabbitmq.addresses", "java.lang.String", null, "Comma-separated list of addresses to which the client should connect to.");
		addPropertyMetadata("spring.rabbitmq.dynamic", "java.lang.Boolean", "true", "Create an AmqpAdmin bean.");
		addPropertyMetadata("spring.rabbitmq.host", "java.lang.String", "localhost", "RabbitMQ host.");
		addPropertyMetadata("spring.rabbitmq.password", "java.lang.String", null, "Login to authenticate against the broker.");
		addPropertyMetadata("spring.rabbitmq.port", "java.lang.Integer", "5672", "RabbitMQ port.");
		addPropertyMetadata("spring.rabbitmq.username", "java.lang.String", null, "Login user to authenticate to the broker.");
		addPropertyMetadata("spring.rabbitmq.virtual-host", "java.lang.String", null, "Virtual host to use when connecting to the broker.");
		addPropertyMetadata("spring.redis.database", "java.lang.Integer", "0", "Database index used by the connection factory.");
		addPropertyMetadata("spring.redis.host", "java.lang.String", "localhost", "Redis server host.");
		addPropertyMetadata("spring.redis.password", "java.lang.String", null, "Login password of the redis server.");
		addPropertyMetadata("spring.redis.pool.max-active", "java.lang.Integer", "8", "Max number of connections that can be allocated by the pool at a given time.\n Use a negative value for no limit.");
		addPropertyMetadata("spring.redis.pool.max-idle", "java.lang.Integer", "8", "Max number of \"idle\" connections in the pool. Use a negative value to indicate\n an unlimited number of idle connections.");
		addPropertyMetadata("spring.redis.pool.max-wait", "java.lang.Integer", "-1", "Maximum amount of time (in milliseconds) a connection allocation should block\n before throwing an exception when the pool is exhausted. Use a negative value\n to block indefinitely.");
		addPropertyMetadata("spring.redis.pool.min-idle", "java.lang.Integer", "0", "Target for the minimum number of idle connections to maintain in the pool. This\n setting only has an effect if it is positive.");
		addPropertyMetadata("spring.redis.port", "java.lang.Integer", "6379", "Redis server port.");
		addPropertyMetadata("spring.redis.sentinel.master", "java.lang.String", null, "Name of Redis server.");
		addPropertyMetadata("spring.redis.sentinel.nodes", "java.lang.String", null, "Comma-separated list of host:port pairs.");
		addPropertyMetadata("spring.resources.add-mappings", "java.lang.Boolean", "true", "Enable default resource handling.");
		addPropertyMetadata("spring.resources.cache-period", "java.lang.Integer", null, "Cache period for the resources served by the resource handler, in seconds.");
		addPropertyMetadata("spring.social.auto-connection-views", "java.lang.Boolean", "false", "Enable the connection status view for supported providers.");
		addPropertyMetadata("spring.social.facebook.app-id", "java.lang.String", null, "Application id.");
		addPropertyMetadata("spring.social.facebook.app-secret", "java.lang.String", null, "Application secret.");
		addPropertyMetadata("spring.social.linkedin.app-id", "java.lang.String", null, "Application id.");
		addPropertyMetadata("spring.social.linkedin.app-secret", "java.lang.String", null, "Application secret.");
		addPropertyMetadata("spring.social.twitter.app-id", "java.lang.String", null, "Application id.");
		addPropertyMetadata("spring.social.twitter.app-secret", "java.lang.String", null, "Application secret.");
		addPropertyMetadata("spring.thymeleaf.cache", "java.lang.Boolean", "true", "Enable template caching.");
		addPropertyMetadata("spring.thymeleaf.check-template-location", "java.lang.Boolean", "true", "Check that the templates location exists.");
		addPropertyMetadata("spring.thymeleaf.content-type", "java.lang.String", "text/html", "Content-Type value.");
		addPropertyMetadata("spring.thymeleaf.enabled", "java.lang.Boolean", "true", "Enable MVC Thymeleaf view resolution.");
		addPropertyMetadata("spring.thymeleaf.encoding", "java.lang.String", "UTF-8", "Template encoding.");
		addPropertyMetadata("spring.thymeleaf.excluded-view-names", "java.lang.String[]", null, "Comma-separated list of view names that should be excluded from resolution.");
		addPropertyMetadata("spring.thymeleaf.mode", "java.lang.String", "HTML5", "Template mode to be applied to templates. See also StandardTemplateModeHandlers.");
		addPropertyMetadata("spring.thymeleaf.prefix", "java.lang.String", "classpath:/templates/", "Prefix that gets prepended to view names when building a URL.");
		addPropertyMetadata("spring.thymeleaf.suffix", "java.lang.String", ".html", "Suffix that gets appended to view names when building a URL.");
		addPropertyMetadata("spring.thymeleaf.view-names", "java.lang.String[]", null, "Comma-separated list of view names that can be resolved.");
		addPropertyMetadata("spring.velocity.allow-request-override", "java.lang.Boolean", null, "Set whether HttpServletRequest attributes are allowed to override (hide) controller\n generated model attributes of the same name.");
		addPropertyMetadata("spring.velocity.cache", "java.lang.Boolean", null, "Enable template caching.");
		addPropertyMetadata("spring.velocity.char-set", "java.lang.String", null, null);
		addPropertyMetadata("spring.velocity.charset", "java.lang.String", null, "Template encoding.");
		addPropertyMetadata("spring.velocity.check-template-location", "java.lang.Boolean", null, "Check that the templates location exists.");
		addPropertyMetadata("spring.velocity.content-type", "java.lang.String", null, "Content-Type value.");
		addPropertyMetadata("spring.velocity.date-tool-attribute", "java.lang.String", null, "Name of the DateTool helper object to expose in the Velocity context of the view.");
		addPropertyMetadata("spring.velocity.enabled", "java.lang.Boolean", null, "Enable MVC view resolution for this technology.");
		addPropertyMetadata("spring.velocity.expose-request-attributes", "java.lang.Boolean", null, "Set whether all request attributes should be added to the model prior to merging\n with the template.");
		addPropertyMetadata("spring.velocity.expose-session-attributes", "java.lang.Boolean", null, "Set whether all HttpSession attributes should be added to the model prior to\n merging with the template.");
		addPropertyMetadata("spring.velocity.expose-spring-macro-helpers", "java.lang.Boolean", null, "Set whether to expose a RequestContext for use by Spring's macro library, under the\n name \"springMacroRequestContext\".");
		addPropertyMetadata("spring.velocity.number-tool-attribute", "java.lang.String", null, "Name of the NumberTool helper object to expose in the Velocity context of the view.");
		addPropertyMetadata("spring.velocity.prefer-file-system-access", "java.lang.Boolean", "true", "Prefer file system access for template loading. File system access enables hot\n detection of template changes.");
		addPropertyMetadata("spring.velocity.prefix", "java.lang.String", null, "Prefix that gets prepended to view names when building a URL.");
		addPropertyMetadata("spring.velocity.properties", "java.util.Map<java.lang.String,java.lang.String>", null, "Additional velocity properties.");
		addPropertyMetadata("spring.velocity.request-context-attribute", "java.lang.String", null, "Name of the RequestContext attribute for all views.");
		addPropertyMetadata("spring.velocity.resource-loader-path", "java.lang.String", "classpath:/templates/", "Template path.");
		addPropertyMetadata("spring.velocity.suffix", "java.lang.String", null, "Suffix that gets appended to view names when building a URL.");
		addPropertyMetadata("spring.velocity.toolbox-config-location", "java.lang.String", null, "Velocity Toolbox config location, for example \"/WEB-INF/toolbox.xml\". Automatically\n loads a Velocity Tools toolbox definition file and expose all defined tools in the\n specified scopes.");
		addPropertyMetadata("spring.velocity.view-names", "java.lang.String[]", null, "White list of view names that can be resolved.");
		addPropertyMetadata("spring.view.prefix", "java.lang.String", null, "Spring MVC view prefix.");
		addPropertyMetadata("spring.view.suffix", "java.lang.String", null, "Spring MVC view suffix.");
	}

}
