package org.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("onClass")
public class TestContextConfigurationCompletion {

    private String value;

    public void method() {
    }
}