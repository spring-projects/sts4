// tag::sample[]
package org.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Employee {

	@Id
	private Long socialSecurityNumber;
	private String firstName;
	private String lastName;

	protected Employee() {
	}

	public Employee(long socialSecurityNumber, String firstName, String lastName) {
		this.socialSecurityNumber = socialSecurityNumber;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return String.format(
				"Employee[socialSecurityNumber=%d, firstName='%s', lastName='%s']",
				socialSecurityNumber, firstName, lastName
		);
	}

// end::sample[]

	public Long getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
