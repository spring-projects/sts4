package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FooConfiguration {

	@Bean
	@ConfigurationProperties("bar")
	public Bar barProperties() {
		return new Bar();
	}

}
