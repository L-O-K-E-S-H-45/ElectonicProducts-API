package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
