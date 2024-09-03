package org.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnResource(resources="onClass")
public class TestConditionalOnResourceCompletion {

    private String value;

    public void method() {
    }
}