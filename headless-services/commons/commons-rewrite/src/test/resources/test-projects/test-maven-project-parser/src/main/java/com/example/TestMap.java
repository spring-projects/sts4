package com.example;

import java.util.LinkedHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.wellsfargo.lendingplatform.web.config.TestObjectWithList;

@Component
@ConfigurationProperties
public class TestMap {

    private LinkedHashMap<String, TestObjectWithList> testMap;

    public LinkedHashMap<String, TestObjectWithList> getTestMap() {
        return testMap;
    }

    public void setTestMap(LinkedHashMap<String, TestObjectWithList> testMap) {
        this.testMap = testMap;
    }

}

