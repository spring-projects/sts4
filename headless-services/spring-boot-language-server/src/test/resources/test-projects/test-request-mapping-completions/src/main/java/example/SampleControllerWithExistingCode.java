package example;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleControllerWithExistingCode {

	private static final String template = "Hello, %s!";

	@GetMapping("/greeting1")
	public String greeting1(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format(template, name);
	}
	
	// <*>
	
	@GetMapping("/greeting2")
	public String greeting2(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format(template, name);
	}

}
