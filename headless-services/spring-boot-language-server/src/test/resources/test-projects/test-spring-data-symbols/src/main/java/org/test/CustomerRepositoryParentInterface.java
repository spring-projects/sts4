package org.test;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomerRepositoryParentInterface extends CrudRepository<Customer, Long> {

	@Query("PARENT REPO INTERFACE QUERY STATEMENT")
	List<Object> findParentPetTypes();
}
