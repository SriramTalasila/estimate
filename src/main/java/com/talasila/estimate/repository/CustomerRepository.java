package com.talasila.estimate.repository;

import com.talasila.estimate.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneAndBusinessId(String phone, Long businessId);
}