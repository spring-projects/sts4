package example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipleConditionals {

	@Bean
	@ConditionalOnBean
	@ConditionalOnWebApplication
	@ConditionalOnJava(value=ConditionalOnJava.JavaVersion.EIGHT)
	@ConditionalOnMissingClass
	@ConditionalOnExpression
	public Hello hi() {
		return null;
	}
}
