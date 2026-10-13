package com.studx.service;

import com.studx.dto.OfferCreateRequest;
import com.studx.dto.OfferResponse;
import com.studx.entity.*;
import com.studx.exception.ConflictException;
import com.studx.exception.UnauthorizedException;
import com.studx.repository.OfferRepository;
import com.studx.repository.ProductRepository;
import com.studx.repository.ReservationAuditLogRepository;
import com.studx.repository.UserRepository;
import com.studx.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationAuditLogRepository auditLogRepository;

    private OfferService offerService;

    private User seller;
    private User buyer;
    private Product product;
    private UserPrincipal sellerPrincipal;
    private UserPrincipal buyerPrincipal;

    @BeforeEach
    void setUp() {
        offerService = new OfferService(offerRepository, productRepository, userRepository, auditLogRepository);

        seller = new User("Seller", "seller@college.edu", "pass", "DU", "Delhi", Role.ROLE_USER);
        seller.setId(1L);

        buyer = new User("Buyer", "buyer@college.edu", "pass", "DU", "Delhi", Role.ROLE_USER);
        buyer.setId(2L);

        Category category = new Category("Electronics", "electronics", "img.png");
        category.setId(5L);

        product = new Product("Calculator", "FX991", BigDecimal.valueOf(900), "img.png", "dims",
                ProductCondition.LIKE_NEW, category, seller);
        product.setId(20L);
        product.setStatus(ProductStatus.AVAILABLE);

        sellerPrincipal = new UserPrincipal(1L, "Seller", "seller@college.edu", "pass", "DU", "Delhi", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        buyerPrincipal = new UserPrincipal(2L, "Buyer", "buyer@college.edu", "pass", "DU", "Delhi", true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testCreateOffer_success() {
        OfferCreateRequest request = new OfferCreateRequest(BigDecimal.valueOf(800), "Can you do 800?");

        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(inv -> {
            Offer o = inv.getArgument(0);
            o.setId(101L);
            return o;
        });

        OfferResponse response = offerService.createOffer(20L, request, buyerPrincipal);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(800), response.getOfferedPrice());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void testAcceptOffer_success_shouldReserveProductAndRejectOtherOffers() {
        Offer acceptedOffer = new Offer(product, buyer, BigDecimal.valueOf(800), "Deal?");
        acceptedOffer.setId(101L);
        acceptedOffer.setStatus(OfferStatus.PENDING);

        User anotherBuyer = new User("Other Buyer", "other@college.edu", "pass", "DU", "Delhi", Role.ROLE_USER);
        anotherBuyer.setId(3L);
        Offer otherOffer = new Offer(product, anotherBuyer, BigDecimal.valueOf(750), "Offer 2");
        otherOffer.setId(102L);
        otherOffer.setStatus(OfferStatus.PENDING);

        when(offerRepository.findById(101L)).thenReturn(Optional.of(acceptedOffer));
        when(productRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(product));
        when(offerRepository.findByProductId(20L)).thenReturn(List.of(acceptedOffer, otherOffer));
        when(offerRepository.save(any(Offer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferResponse response = offerService.acceptOffer(101L, sellerPrincipal);

        assertEquals("ACCEPTED", response.getStatus());
        assertEquals(ProductStatus.RESERVED, product.getStatus());
        assertEquals(OfferStatus.REJECTED, otherOffer.getStatus());

        verify(auditLogRepository).save(any(ReservationAuditLog.class));
    }

    @Test
    void testAcceptOffer_byNonSeller_shouldThrowUnauthorizedException() {
        Offer offer = new Offer(product, buyer, BigDecimal.valueOf(800), "Deal?");
        offer.setId(101L);

        when(offerRepository.findById(101L)).thenReturn(Optional.of(offer));
        when(productRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class, () -> offerService.acceptOffer(101L, buyerPrincipal));
    }

    @Test
    void testAcceptOffer_alreadyReservedProduct_shouldThrowConflictException() {
        Offer offer = new Offer(product, buyer, BigDecimal.valueOf(800), "Deal?");
        offer.setId(101L);
        product.setStatus(ProductStatus.RESERVED);

        when(offerRepository.findById(101L)).thenReturn(Optional.of(offer));
        when(productRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> offerService.acceptOffer(101L, sellerPrincipal));
    }
}
