package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.Seller;

public interface SellerRepository extends JpaRepository<Seller, Integer> {

}
