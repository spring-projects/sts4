package org.springframework.ide.si.view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SprottySiViewApplication {
	
	@Bean
	GraphDataProvider mockGraphDataProvider() {
		return new MockGraphData();
	}
	
//	@Bean
//	GraphDataProvider graphDataProvider() {
//		
//	}

	public static void main(String[] args) {
		SpringApplication.run(SprottySiViewApplication.class, args);
	}

}
