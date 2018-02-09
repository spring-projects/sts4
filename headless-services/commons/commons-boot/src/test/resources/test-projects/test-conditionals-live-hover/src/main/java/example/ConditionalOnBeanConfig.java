package example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalOnBeanConfig {

	@Bean
	@ConditionalOnBean
	public Hello hi() {
		return null;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public Hello missing() {
		return null;
	}
}
