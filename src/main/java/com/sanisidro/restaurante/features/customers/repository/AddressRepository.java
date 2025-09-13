package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);

    Page<Address> findByCustomerId(Long customerId, Pageable pageable);

    boolean existsByCustomerAndAddress(Customer customer, String address);
}
