package com.studx.repository;

import com.studx.entity.Product;
import com.studx.entity.ProductCondition;
import com.studx.entity.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsFeaturedTrueAndStatus(ProductStatus status);

    List<Product> findByIsDealTrueAndStatus(ProductStatus status);

    List<Product> findBySellerId(Long sellerId);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:condition IS NULL OR p.conditionGrade = :condition) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("condition") ProductCondition condition,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("status") ProductStatus status,
                                 Pageable pageable);

    @Query("SELECT AVG(p.price), MIN(p.price), MAX(p.price), COUNT(p) FROM Product p " +
           "WHERE p.category.id = :categoryId AND (:condition IS NULL OR p.conditionGrade = :condition)")
    List<Object[]> findPriceStatsByCategoryAndCondition(@Param("categoryId") Long categoryId,
                                                        @Param("condition") ProductCondition condition);
}
