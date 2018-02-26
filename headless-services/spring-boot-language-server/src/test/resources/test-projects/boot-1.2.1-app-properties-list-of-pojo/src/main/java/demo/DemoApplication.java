package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    FooProperties props;
    
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Hello world!");
		for (Foo foo : props.getList()) {
			System.out.println("===================");
			System.out.println("name = "+foo.getName());
			System.out.println("desc = "+foo.getDescription());
			for (String string : foo.getRoles()) {
				System.out.println("  role: "+string);
			}
		}
	}
}
