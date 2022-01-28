package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoConfImportApplication implements CommandLineRunner {
	
	@Value("some.extra.stuff")
	String prop;

	public static void main(String[] args) {
		SpringApplication.run(DemoConfImportApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("prop = "+prop);
	}

}
