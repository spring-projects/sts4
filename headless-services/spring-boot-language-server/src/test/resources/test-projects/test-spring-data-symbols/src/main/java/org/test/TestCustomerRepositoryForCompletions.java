package org.test;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
}
