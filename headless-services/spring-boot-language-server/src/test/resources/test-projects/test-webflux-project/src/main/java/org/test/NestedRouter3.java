package org.test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class NestedRouter3 {
	
	@Bean
	public RouterFunction<ServerResponse> routingFunction() {
		PersonHandler3 handler = new PersonHandler3();

		return nest(path("/person"),
				nest(path("/sub1"),
				  nest(path("/sub2"),
				    nest(accept(APPLICATION_JSON),
					  route(GET("/{id}"), handler::getPerson)
					  .andRoute(method(HttpMethod.GET), handler::listPeople))
				    .andRoute(GET("/nestedGet"), handler::getPerson))
				  .and(nest(path("/andNestPath"),
					route(GET("/andNestPathGET"), handler::getPerson))))
				.andRoute(POST("/").and(contentType(APPLICATION_JSON)), handler::createPerson))
			  .and(nest(method(HttpMethod.DELETE), route(path("/nestedDelete"), handler::deletePerson)));
	}

}
