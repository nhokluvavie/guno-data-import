package com.guno.etl.repository;

import com.guno.etl.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    /**
     * Find customer by phone hash for privacy-safe lookup
     */
    Optional<Customer> findByPhoneHash(String phoneHash);

    /**
     * Find customer by platform-specific customer ID
     */
    Optional<Customer> findByPlatformCustomerId(String platformCustomerId);

    /**
     * Check if customer exists by phone hash
     */
    boolean existsByPhoneHash(String phoneHash);

    /**
     * Find next available customer key for new customers
     */
    @Query("SELECT COALESCE(MAX(c.customerKey), 0) + 1 FROM Customer c")
    Long findNextCustomerKey();
}