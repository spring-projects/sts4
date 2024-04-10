package org.test.inheritance;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(
		path = "/superinterface-path",
		method = {GET, POST},
		produces = {MediaType.TEXT_PLAIN_VALUE},
		consumes = {"testconsume"})
public interface SuperInterfaceWithMappingPath {
}
