package com.example;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;

public class MyBeanRegistrar implements BeanRegistrar {

	@Override
	public void register(BeanRegistry registry, Environment env) {
		registry.registerBean(FooFoo.class);
		registry.registerBean("foo", Foo.class);
		registry.registerBean("bar", Bar.class, spec -> spec.prototype().lazyInit().description("Custom description")
				.supplier(context -> new Bar(context.bean(Foo.class))));
		if (env.matchesProfiles("baz")) {
			registry.registerBean(Baz.class, spec -> spec.supplier(context -> new Baz("Hello World!")));
		}
	}

}
