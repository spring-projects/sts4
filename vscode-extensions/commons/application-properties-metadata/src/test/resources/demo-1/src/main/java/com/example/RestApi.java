package com.example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApi {
	
	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "hello!";
	}

}
