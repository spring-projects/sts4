package demo;

public class TrickyGetters {

	private static String staticProperty;
	private String privateProperty;
	private String publicProperty;

	private String getPrivateProperty() {
		return privateProperty;
	}

	private void setPrivateProperty(String privateProperty) {
		this.privateProperty = privateProperty;
	}

	public static String getStaticProperty() {
		return staticProperty;
	}

	public static void setStaticProperty(String staticProperty) {
		TrickyGetters.staticProperty = staticProperty;
	}

	public String getPublicProperty() {
		return publicProperty;
	}

	public void setPublicProperty(String publicProperty) {
		this.publicProperty = publicProperty;
	}


}
