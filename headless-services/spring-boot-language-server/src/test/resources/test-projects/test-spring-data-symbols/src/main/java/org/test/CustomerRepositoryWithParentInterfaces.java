package org.test;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface CustomerRepositoryWithParentInterfaces extends CustomerRepositoryParentInterface {

	@Query("CONCRETE REPO SELECT STATEMENT")
	List<Object> findConcretePetTypes();
}
