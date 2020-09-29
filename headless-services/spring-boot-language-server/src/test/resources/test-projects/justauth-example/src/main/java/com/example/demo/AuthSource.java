package com.example.demo;

public interface AuthSource {

	String authorize();

	String accessToken();

	String userInfo();

}
