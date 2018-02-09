package example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionalOnMissingBeanConfig {
	
	@Bean
	@ConditionalOnMissingBean
	public Hello missing() {
		return null;
	}
}
