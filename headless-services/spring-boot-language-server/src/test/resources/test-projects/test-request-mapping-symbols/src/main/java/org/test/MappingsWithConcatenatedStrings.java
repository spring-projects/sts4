package org.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import org.springframework.stereotype.Controller;

@Controller
public class MappingsWithConcatenatedStrings {
	
	@GetMapping("/path1" + "/path2")
	public void concatenatedPathMapping() {
	}

	@GetMapping("/path1/" + Constants.REQUEST_MAPPING_PATH)
	public void concatenatedPathMappingWithConstant() {
	}

}
