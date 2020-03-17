package test;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties
public class NestedConfigProperties {

	private String dude;
	private String bob;

	@ConstructorBinding
	public NestedConfigProperties(String dude) {
		this.dude = dude;
	}

	public String getBob() {
		return bob;
	}

	public void setBob(String bob) {
		this.bob = bob;
	}
	
	public String getDude() {
		return dude;
	}
}