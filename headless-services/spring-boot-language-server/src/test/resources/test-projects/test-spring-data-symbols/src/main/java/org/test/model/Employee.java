// tag::sample[]
package org.test.model;

import javax.persistence.Entity;
import javax.persistence.Id;

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
		return String.format(
				"Employee[socialSecurityNumber=%d, firstName='%s', lastName='%s']",
				socialSecurityNumber, firstName, lastName
		);
	}

// end::sample[]

	public Long getSocialSecurityNumber() {
		return socialSecurityNumber;
	}

}
