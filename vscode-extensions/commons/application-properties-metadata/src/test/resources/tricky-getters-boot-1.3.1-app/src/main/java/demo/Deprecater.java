package demo;

import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

public class Deprecater {

	private String newName;
	@Deprecated private String name;

	private String altName;

	///////////////////


	@Deprecated
	public String getName() {
		return name;
	}

	@Deprecated
	public void setName(String oldName) {
		this.name = oldName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public void setAltName(String name) {
		altName = name;
	}

	@DeprecatedConfigurationProperty(reason="No good anymore", replacement="something.else")
	public String getAltName() {
		return altName;
	}

}
