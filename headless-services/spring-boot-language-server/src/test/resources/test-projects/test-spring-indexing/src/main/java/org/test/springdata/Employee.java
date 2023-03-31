// tag::sample[]
package org.test.springdata;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Employee extends Person {

	@Id
	private Long socialSecurityNumber;

	protected Employee() {
	}

	public Employee(long socialSecurityNumber, String firstName, String lastName) {
		super(firstName, lastName);
		this.socialSecurityNumber = socialSecurityNumber;
	}

	@Override
	public String toString() {
		return "Employee[socialSecurityNumber=%d, firstName='%s', lastName='%s']".formatted(
                socialSecurityNumber, firstName, lastName);
	}

// end::sample[]

	public Long getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

}
