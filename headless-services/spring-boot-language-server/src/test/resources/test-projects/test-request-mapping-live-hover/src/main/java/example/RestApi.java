package example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Boot Java - Test Completion */
@Controller
public class RestApi {


	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "Hello there!";
	}
	
	
	@RequestMapping("/goodbye")
	@ResponseBody
	public String goodbye() {
		return "Good bye";
	}

	@GetMapping("/person/{name}")
	public String getMapping(@PathVariable String name) {
		return "Hello " + name;
	}

	@DeleteMapping("/delete/{id}")
	public String removeMe(@PathVariable int id) {
		System.out.println("You are removed: " + id);
		return "Done";
	}

	@PostMapping("/postHello")
	public String postMethod(@RequestBody String name) {
		System.out.println("Posted hello: " + name);
		return name;
	}

	@PutMapping("/put/{id}")
	public String putMethod(@PathVariable int id, @RequestBody String name) {
		System.out.println("Added " + name + " with ID: " + id);
		return name;
	}
}
