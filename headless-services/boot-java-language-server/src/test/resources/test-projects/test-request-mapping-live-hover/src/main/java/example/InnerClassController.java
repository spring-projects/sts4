package example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class InnerClassController {
	
	@RestController
	public static class InnerController {
		
	    @RequestMapping("/inner-class")
	    public String saySomething() {
	        return "Hello from Inner-Class";
	    }
	    
	    @RestController
	    public static class InnerInnerController {
	    	
		    @RequestMapping("/inner-inner-class")
		    public String saySomethingSuperInnerClass() {
		        return "Hello from Inner-Inner-Class";
		    }
		    
	    }
		
	}

}
