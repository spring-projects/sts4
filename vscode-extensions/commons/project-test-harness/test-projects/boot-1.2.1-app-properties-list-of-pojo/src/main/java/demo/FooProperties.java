package demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties("volder.foo")
@Configuration
public class FooProperties {

	private List<Foo> list;

	public List<Foo> getList() {
		return list;
	}

	public void setList(List<Foo> foo) {
		this.list = foo;
	}

}
