package org.test.model;

public class Person {
	
	protected String firstName;
	protected String lastName;
	
	protected Person() {
		
	}
	
	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
}
