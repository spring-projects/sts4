package example;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Value;

@RestController
public class SampleControllerWithRandomCode {

	private static final String template = "Hello, %s!";
	
	@Value("${getValueProperty}")
	private String someValue;

	@GetMapping("/greeting1")
	public GetSomeService getSomethingMethod(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new GetSomeService();
	}
	
}
