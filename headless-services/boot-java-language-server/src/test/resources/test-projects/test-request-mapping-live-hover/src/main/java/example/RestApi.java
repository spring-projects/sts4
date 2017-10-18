package example;

import org.springframework.stereotype.Controller;
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
}
