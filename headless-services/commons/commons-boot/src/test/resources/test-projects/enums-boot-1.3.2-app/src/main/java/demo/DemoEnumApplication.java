package demo;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DemoEnumApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoEnumApplication.class, args);
    }

    
    @Autowired
	@Value("${server.port}")
    FooProperties foo; 
    
    @Override
    public void run(String... arg0) throws Exception {
    	System.out.println("Today I'm feeling... "+foo.getColor());
    	
    	System.out.println("---- name-colors ----");
    	for (Entry<String, Color> e : foo.getNameColors().entrySet()) {
			System.out.println(e.getKey() +" -> "+ e.getValue());
		}
    	
    	System.out.println("---- color-names ----");
    	for (Entry<Color, String> e : foo.getColorNames().entrySet()) {
			System.out.println(e.getKey() +" -> "+ e.getValue());
		}

    	System.out.println("---- list ----");
    	for (String string : foo.getList()) {
			System.out.println(string);
		}
    	
    }
}
