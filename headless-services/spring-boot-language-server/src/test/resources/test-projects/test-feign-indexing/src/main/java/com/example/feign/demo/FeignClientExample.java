package com.example.feign.demo;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "stores", configuration = FeignConfigExample.class)
public interface FeignClientExample {

	@RequestMapping(method = RequestMethod.GET, value = "/stores")
	List<String> getStores();

}
