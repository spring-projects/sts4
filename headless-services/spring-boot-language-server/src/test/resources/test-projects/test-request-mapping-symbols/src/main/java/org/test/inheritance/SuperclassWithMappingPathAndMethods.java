package org.test.inheritance;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(
		path = "/superclasspath",
		method = {POST, PUT},
		produces = {MediaType.TEXT_PLAIN_VALUE},
		consumes = {"testconsume"})
public class SuperclassWithMappingPathAndMethods {
}
