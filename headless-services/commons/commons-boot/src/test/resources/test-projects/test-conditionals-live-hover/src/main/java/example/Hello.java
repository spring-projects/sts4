package example;


public class Hello {

	private String hello = "Hello";

	public Hello(String hello) {
		this.hello = hello;
	}

	public String sayHello() {
		return hello;
	}

	public static Hello say(String hello) {
		return new Hello(hello + " individual");
	}

}
