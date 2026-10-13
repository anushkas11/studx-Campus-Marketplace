package com.studx.repository;

import com.studx.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByProductId(Long productId);
    List<Offer> findByBuyerId(Long buyerId);
    List<Offer> findByProductSellerId(Long sellerId);
}
