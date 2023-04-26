package com.example.demo;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app2")
public class Settings2 {

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
    public static class Service {
        private Endpoint service;
        private boolean value;
        public Endpoint getService() {
            return service;
        }
        public void setService(Endpoint service) {
            this.service = service;
        }
        public boolean isValue() {
            return value;
        }
        public void setValue(boolean value) {
            this.value = value;
        }
    }


    private Service service1;
    private boolean value;

    public Service getService1() {
        return service1;
    }

    public void setService1(Service service1) {
        this.service1 = service1;
    }


    public boolean isValue() {
        return value;
    }


    public void setValue(boolean value) {
        this.value = value;
    }
}
