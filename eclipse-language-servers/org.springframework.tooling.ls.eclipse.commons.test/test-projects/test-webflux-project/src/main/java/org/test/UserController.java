package org.test;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

	@GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Object> listUsers() {
		return null;
	}

	@GetMapping(path = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Object> showUsers(@PathVariable String username) {
		return null;
	}

}