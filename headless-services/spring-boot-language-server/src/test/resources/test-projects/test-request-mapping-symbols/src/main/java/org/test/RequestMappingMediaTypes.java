package org.test;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class RequestMappingMediaTypes {

	@RequestMapping(path="/consume1", consumes = "testconsume", method= {RequestMethod.HEAD})
	public String consume1() {
		return "Hello";
	}
	
	@RequestMapping(path="/consume2", consumes = MediaType.TEXT_PLAIN_VALUE)
	public String consume2() {
		return "Hello";
	}
	
	@RequestMapping(path="/consume3", consumes = {MediaType.TEXT_PLAIN_VALUE, "testconsumetype"})
	public String consume3() {
		return "Hello";
	}
	
	@RequestMapping(path="/produce1", produces = "testproduce")
	public String produce1() {
		return "Hello";
	}
	
	@RequestMapping(path="/produce2", produces = MediaType.TEXT_PLAIN_VALUE)
	public String produce2() {
		return "Hello";
	}
	
	@RequestMapping(path="/produce3", produces = {MediaType.TEXT_PLAIN_VALUE, "testproducetype"})
	public String produce3() {
		return "Hello";
	}
	
	@RequestMapping(path="/everything", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, "testconsume"}, produces=MediaType.APPLICATION_JSON_VALUE)
	public String everything() {
		return "Hello";
	}

}
