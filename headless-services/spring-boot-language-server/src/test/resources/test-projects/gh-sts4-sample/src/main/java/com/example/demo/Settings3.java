package com.example.demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app3")
public class Settings3 {
    public static class Endpoint {
        private String host;
        private int port;
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public int getPort() {
            return port;
        }
        public void setPort(int port) {
            this.port = port;
        }
        
    }

    private Endpoint service1;
    private Endpoint service2;
    private boolean value;

    public Endpoint getService1() {
        return service1;
    }
    public void setService1(Endpoint service1) {
        this.service1 = service1;
    }
    public Endpoint getService2() {
        return service2;
    }
    public void setService2(Endpoint service2) {
        this.service2 = service2;
    }
    public boolean isValue() {
        return value;
    }
    public void setValue(boolean value) {
        this.value = value;
    }
}
