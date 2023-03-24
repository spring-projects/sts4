// tag::sample[]
package org.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Customer extends Person {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private boolean thisCustomerIsSpecial;//contains keyword in name

	@ManyToOne
	private Employee responsibleEmployee;

	protected Customer() {
		super();
	}

	public Customer(String firstName, String lastName, Employee responsibleEmployee) {
		super(firstName, lastName);
		this.responsibleEmployee = responsibleEmployee;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", responsibleEmployee="
				+ responsibleEmployee + "]";
	}

// end::sample[]

	public Long getId() {
		return id;
	}

	public boolean isThisCustomerIsSpecial() {
		return thisCustomerIsSpecial;
	}

	public Employee getResponsibleEmployee() {
		return responsibleEmployee;
	}
}
