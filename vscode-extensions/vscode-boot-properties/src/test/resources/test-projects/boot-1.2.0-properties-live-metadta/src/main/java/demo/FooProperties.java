package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author Stephane Nicoll
 */
@ConfigurationProperties("foo")
public class FooProperties {

	private String name;

	private String description;

	/**
	 * A nice counter description.
	 */
	private Integer counter = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(Integer counter) {
		this.counter = counter;
	}
}
