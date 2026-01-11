package io.smartpos.infrastructure.dao;

import io.smartpos.core.domain.customer.Customer;
import java.util.List;

public interface CustomerDao {
    void save(Customer customer);

    Customer findById(int id);

    Customer findByDocument(String document);

    List<Customer> findAllActive();
}
