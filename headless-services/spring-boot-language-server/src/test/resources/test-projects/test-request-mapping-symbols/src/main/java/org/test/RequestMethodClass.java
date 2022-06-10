package org.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod.*;

public class RequestMethodClass {
	
	@GetMapping("/getData")
	public void getData() {
	}
	
	@PutMapping("/putData")
	public void putData() {
	}

	@DeleteMapping("/deleteData")
	public void deleteData() {
	}

	@PostMapping("/postData")
	public void postData() {
	}

	@PatchMapping("/patchData")
	public void patchData() {
	}
	
	@RequestMapping(value="/getHello", method=RequestMethod.GET)
	public getHello() {
	}

	@RequestMapping(path="/postAndPutHello", method= {RequestMethod.POST, PUT})
	public void updateHello() {
	}

	@GetMapping("")
	public void getDataWithoutPath() {
	}
	
	@GetMapping()
	public void getDataWithoutAnything() {
	}
	

}
