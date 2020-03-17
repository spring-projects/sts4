package test;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "some-config")
public class ConfigProperties {

	public final NestedConfigProperties n1;
	public final NestedConfigProperties n2;

	@ConstructorBinding
	public ConfigProperties(NestedConfigProperties nested, NestedConfigProperties nested2) {
		this.n1 = nested;
		this.n2 = nested;
	}
}