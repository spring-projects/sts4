package hello;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void corsWithAnnotation() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ORIGIN, "http://localhost:9000");
        HttpEntity<?> requestEntity = new HttpEntity(headers);
        ResponseEntity<Greeting> entity = this.restTemplate.exchange("/greeting", HttpMethod.GET, requestEntity, Greeting.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
        Greeting greeting = entity.getBody();
        assertEquals("Hello, World!", greeting.getContent());
    }

    @Test
    public void corsWithJavaconfig() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ORIGIN, "http://localhost:9000");
        HttpEntity<?> requestEntity = new HttpEntity(headers);
        ResponseEntity<Greeting> entity = this.restTemplate.exchange("/greeting-javaconfig", HttpMethod.GET, requestEntity, Greeting.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("http://localhost:9000", entity.getHeaders().getAccessControlAllowOrigin());
        Greeting greeting = entity.getBody();
        assertEquals("Hello, World!", greeting.getContent());
    }

}