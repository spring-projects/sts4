package com.java.data;

import java.util.List;

/**
 * User properties as defined.
 */
public record UserPropertiesRecord(String name, String password, List<String> roles) {

}