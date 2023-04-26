package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class Settings {
    public static record Endpoint(String host, int port) {}
    public static record Service(Endpoint service) {}
    
    
    private Service service1;
    public Service getService1() {
        return service1;
    }
    public void setService1(Service service1) {
        this.service1 = service1;
    }
}