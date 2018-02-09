package example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipleConditionalsPT152535713 {

	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)
	public Hello hi() {
		return null;
	}
}
