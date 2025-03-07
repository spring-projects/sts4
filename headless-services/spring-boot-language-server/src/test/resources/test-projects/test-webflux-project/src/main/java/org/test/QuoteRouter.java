package org.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class QuoteRouter {

	@Bean
	public RouterFunction<ServerResponse> route(QuoteHandler quoteHandler) {
		return RouterFunctions
				.route(GET("/hello").and(accept(TEXT_PLAIN)), quoteHandler::hello)
				.andRoute(POST("/echo").and(accept(TEXT_PLAIN).and(contentType(TEXT_PLAIN))), quoteHandler::echo)
				.andRoute(GET("/quotes").and(accept(APPLICATION_JSON)), quoteHandler::fetchQuotes)
				.andRoute(RequestPredicates.GET("/quotes").and(accept(MediaType.APPLICATION_STREAM_JSON)), quoteHandler::streamQuotes);
	}
}