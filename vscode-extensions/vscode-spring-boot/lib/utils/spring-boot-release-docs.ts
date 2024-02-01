export const releaseNotes = `Spring Boot 3.0 Release Notes

Upgrading from Spring Boot 2.7
Since this is a major release of Spring Boot, upgrading existing applications can be a little more involved that usual. We’ve put together a dedicated migration guide to help you upgrade your existing Spring Boot 2.7 applications.

If you’re currently running with an earlier version of Spring Boot, we strongly recommend that you upgrade to Spring Boot 2.7 before migrating to Spring Boot 3.0.

New and Noteworthy
Tip
Check the configuration changelog for a complete overview of the changes in configuration.
Java 17 Baseline and Java 19 Support
Spring Boot 3.0 requires Java 17 as a minimum version. If you are currently using Java 8 or Java 11, you’ll need to upgrade your JDK before you can develop Spring Boot 3.0 applications.

Spring Boot 3.0 also works well, and has been tested with JDK 19.

GraalVM Baseline and Native Build Tools
Spring Boot requires Graal 22.3 or later and Native Build Tools Plugin 0.9.17 or later

Third-party Library Upgrades
Spring Boot 3.0 builds on and requires Spring Framework 6. You might like to read about the new features available in Spring Framework 6.0.

Other Spring projects upgraded in this release include:

Spring AMQP 3.0.

Spring Batch 5.0.

Spring Data 2022.0.

Spring GraphQL 1.1.

Spring HATEOAS 2.0.

Spring Integration 6.0.

Spring Kafka 3.0.

Spring LDAP 3.0.

Spring REST Docs 3.0.

Spring Retry 2.0.

Spring Security 6.0 (see also what’s new).

Spring Session 3.0

Spring WS 4.0.

Spring Boot 3.0 has migrated from Java EE to Jakarta EE APIs for all dependencies. Wherever possible, Jakarta EE 10 compatible dependencies have been chosen, including:

Jakarta Activation 2.1

Jakarta JMS 3.1

Jakarta JSON 2.1

Jakarta JSON Bind 3.0

Jakarta Mail 2.1

Jakarta Persistence 3.1

Jakarta Servlet 6.0

Jakarta Servlet JSP JSTL 3.0

Jakarta Transaction 2.0

Jakarta Validation 3.0

Jakarta WebSocket 2.1

Jakarta WS RS 3.1

Jakarta XML SOAP 3.0

Jakarta XML WS 4.0

We’ve also upgraded to the latest stable releases of third-party jars wherever possible. Some notable dependency upgrades here include:

Couchbase Client 3.4

Ehcache 3.10

Elasticsearch Client 8.5

Flyway 9

Groovy 4.0

Hibernate 6.1

Hibernate Validator 8.0

Jackson 2.14

Jersey 3.1

Jetty 11

jOOQ 3.16

Kotlin 1.7.20

Liquibase 4.17

Lettuce 6.2

Log4j 2.18

Logback 1.4

Micrometer 1.10

Micrometer Tracing 1.0

Neo4j Java Driver 5.2

Netty 4.1.77.Final

OkHttp 4.10

R2DBC 1.0

Reactor 2022.0

SLF4J 2.0

SnakeYAML 1.32

Tomcat 10

Thymeleaf 3.1.0.M2

Undertow 2.2.20.Final

GraalVM Native Image Support
Spring Boot 3.0 applications can now be converted into GraalVM native images which can provide significant memory and startup-up performance improvements. Supporting GraalVM Native Images has been a major engineering effort undertaken across the entire Spring portfolio.

To get started with GraalVM native images, please see the updated Spring Boot reference documentation.

Log4j2 Enhancements
Log4j2 support has been updated with new extensions that provide the following functionality:

Profile-specific Configuration

Environment Properties Lookup

Log4j2 System Properties

For details, please see the updated documentation.

Improved @ConstructorBinding Detection
When using constructor bound @ConfigurationProperties the @ConstructorBinding annotation is no longer required if the class has a single parameterized constructor. If you have more than one constructor, you’ll still need to use @ConstructorBinding to tell Spring Boot which one to use.

For most users, this updated logic will allow for simpler @ConfigurationProperties classes. If, however, you have a @ConfigurationProperties and you want to inject beans into the constructor rather than binding it, you’ll now need to add an @Autowired annotation.

Micrometer Updates
Auto-configuration for Micrometer Observation API
Spring Boot 3.0 supports the new observation APIs introduced in Micrometer 1.10. The new ObservationRegistry interface can be used to create observations which provides a single API for both metrics and traces. Spring Boot now auto-configures an instance of ObservationRegistry for you.

If micrometer-core is on the classpath, a DefaultMeterObservationHandler is registered on the ObservationRegistry, which means that every stopped Observation leads to a timer. ObservationPredicate, GlobalObservationConvention and ObservationHandler are automatically registered on the ObservationRegistry. You can use ObservationRegistryCustomizer to further customize the ObservationRegistry if you need to.

For more details see the new 'Observability' section of the reference documentation.

Auto-configuration for Micrometer Tracing
Spring Boot now auto-configures Micrometer Tracing for you. This includes support for Brave, OpenTelemetry, Zipkin and Wavefront.

When using the Micrometer Observation API, finishing observations will lead to spans reported to Zipkin or Wavefront. Tracing can be controlled with properties under management.tracing. Zipkin can be configured with management.zipkin.tracing, while Wavefront uses management.wavefront.

More details, including the various dependencies that you’ll need to add, are in the tracing section of the reference documentation.

Auto-configuration for Micrometer’s OtlpMeterRegistry
An OtlpMeterRegistry is now auto-configured when io.micrometer:micrometer-registry-otlp is on the classpath. The meter registry can be configured using management.otlp.metrics.export.* properties.

Prometheus Support
Auto-Configuration for Prometheus Exemplars
When there is a Micrometer Tracing Tracer bean and Prometheus is on the classpath, a SpanContextSupplier is now auto-configured. This supplier links metrics to traces by making the current trace ID and span ID available to Prometheus.

Making a PUT to Prometheus Push Gateway on Shutdown
The Push Gateway can be configured to perform a PUT on shutdown. To do so, set management.prometheus.metrics.export.pushgateway.shutdown-operation to put. Additionally, the existing push setting has been deprecated and post should now be used instead.

More Flexible Auto-configuration for Spring Data JDBC
The auto-configuration for Spring Data JDBC is now more flexible. Several auto-configured beans that are required by Spring Data JDBC are now conditional and can be replaced by defining a bean of the same type. The types of the beans that can now be replaced are the following:

org.springframework.data.jdbc.core.JdbcAggregateTemplate

org.springframework.data.jdbc.core.convert.DataAccessStrategy

org.springframework.data.jdbc.core.convert.JdbcConverter

org.springframework.data.jdbc.core.convert.JdbcCustomConversions

org.springframework.data.jdbc.core.mapping.JdbcMappingContext

org.springframework.data.relational.RelationalManagedTypes

org.springframework.data.relational.core.dialect.Dialect

Enabling Async Acks with Apache Kafka
A new configuration property, spring.kafka.listener.async-acks, for enabling async acks with Kafka has been added. To enable async acks, set the property to true. The property only applies when spring.kafka.listener.async-mode is set to manual or manual-immediate.

Elasticsearch Java Client
Auto-configuration for the new Elasticsearch Java Client has been introduced. It can be configured using the existing spring.elasticsearch.* configuration properties.

The auto-configuration for the new client does not use the auto-configured ObjectMapper for JSON mapping. This is to prevent clashes between the needs of the application and the needs of Elasticsearch. To control the ObjectMapper that is used, define a JacksonJsonpMapper bean. For example, the following configuration will result in the Elastic client using the context’s ObjectMapper:

@Bean
JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
	return new JacksonJsonpMapper(objectMapper);
}
Auto-configuration of JdkClientHttpConnector
In the absence of Reactor Netty, Jetty’s reactive client, and the Apache HTTP client a JdkClientHttpConnector will now be auto-configured. This allows WebClient to be used with the JDK’s HttpClient.

@SpringBootTest with Main Methods
The @SpringBootTest annotation can now use the main of any discovered @SpringBootConfiguration class if it’s available. This means that any custom SpringApplication configuration performed by your main method can now be picked up by tests.

To use the main method for a test set the useMainMethod attribute of @SpringBootTest to UseMainMethod.ALWAYS or UseMainMethod.WHEN_AVAILABLE.

See the updated reference documentation for details.

Miscellaneous
Apart from the changes listed above, there have also been some minor tweaks and improvements including:

Host names are no longer logged during application startup. This prevents a network lookup which helps improve startup times.

Support for Java’s SecurityManager has been removed following its deprecation in the JDK.

Support for Spring Framework’s CommonsMultipartResolver has been removed following its removal in Spring Framework 6.

The spring.mvc.ignore-default-model-on-redirect has been deprecated to align with upstream Spring Framework changes.

WebJars resource handler path pattern can be customized using spring.mvc.webjars-path-pattern or spring.webflux.webjars-path-pattern.

Trusted proxies of Tomcat’s remote IP valve can be configured using server.tomcat.remoteip.trusted-proxies.

Bean Validation Configuration can now be customised by defining a ValidationConfigurationCustomizer bean.

Log4j2’s Log4jBridgeHandler is now used to route JUL-based logging into Log4j2 rather than routing through SLF4J.

Bean implementing the MeterBinder interface are now bound to meter registries only after all singleton beans have been initialized.

SpanCustomizer beans for Brave and OpenTelemetry are now auto-configured.

Micrometer’s JvmCompilationMetrics are now auto-configured.

DiskSpaceHealthIndicator now includes its path in its log message and its health details.

A DataSourceBuilder can now be derived from a wrapped DataSource.

Multiple hosts can now be configured for MongoDB using the spring.data.mongodb.additional-hosts property.

Elasticsearch’s socketKeepAlive property can be configured using the spring.elasticsearch.socket-keep-alive property.

When using spring-rabbit-stream, a RabbitStreamTemplate and Environment will now be auto-configured whether or not spring.rabbitmq.listener.type is stream.

Existing Kafka topics can be modified using spring.kafka.admin.modify-topic-configs.

WebDriverScope and WebDriverTestExecutionListener have been made public to ease the use of WebDriver in custom test setup.

Deprecations in Spring Boot 3.0
@ConstructorBinding has been relocated from the org.springframework.boot.context.properties package to org.springframework.boot.context.properties.bind.

JsonMixinModule scanning based constructor has been deprecated.

ClientHttpRequestFactorySupplier should be replaced with ClientHttpRequestFactories.

Cookie comment properties are no longer supported.

RestTemplateExchangeTagsProvider, WebClientExchangeTagsProvider, WebFluxTagsProvider, WebMvcTagsProvider and related classes have been replaced with ObservationConvention equivalents.

The no-args constructors on HealthContributor @Configuration base classes have been deprecated.

DefaultTestExecutionListenersPostProcessor and SpringBootDependencyInjectionTestExecutionListener have been deprecated in favor of Spring Framework’s ApplicationContextFailureProcessor.

The properties management.metrics.export.<product> are deprecated, the replacement is management.<product>.metrics.export.

The push setting of management.prometheus.metrics.export.pushgateway.shutdown-operation in favor of post.

@AutoConfigureMetrics has been deprecated in favor of @AutoConfigureObservability.`;