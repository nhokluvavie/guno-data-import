package com.guno.etl.repository;

import com.guno.etl.entity.Product;
import com.guno.etl.entity.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId> {

    /**
     * Find product by SKU only (across all platforms)
     */
    List<Product> findBySku(String sku);

    /**
     * Find products by brand
     */
    List<Product> findByBrand(String brand);

    /**
     * Find active products
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find featured products
     */
    List<Product> findByIsFeaturedTrue();

    /**
     * Find products by category level 1
     */
    List<Product> findByCategoryLevel1(String categoryLevel1);

    /**
     * Find products in price range
     */
    @Query("SELECT p FROM Product p WHERE p.retailPrice BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * Search products by name (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> searchByProductName(@Param("name") String name);

    /**
     * Check if product exists by platform product ID
     */
    boolean existsByPlatformProductId(String platformProductId);
}