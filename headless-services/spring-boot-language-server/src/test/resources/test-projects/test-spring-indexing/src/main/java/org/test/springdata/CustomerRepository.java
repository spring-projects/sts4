package org.test.springdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;

@Qualifier("repoQualifier")
@Profile({"prof1", "prof2"})
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);
    
}
