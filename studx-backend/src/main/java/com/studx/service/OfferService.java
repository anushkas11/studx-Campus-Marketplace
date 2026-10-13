package com.studx.service;

import com.studx.dto.OfferCreateRequest;
import com.studx.dto.OfferResponse;
import com.studx.entity.*;
import com.studx.exception.BadRequestException;
import com.studx.exception.ConflictException;
import com.studx.exception.ResourceNotFoundException;
import com.studx.exception.UnauthorizedException;
import com.studx.repository.OfferRepository;
import com.studx.repository.ProductRepository;
import com.studx.repository.ReservationAuditLogRepository;
import com.studx.repository.UserRepository;
import com.studx.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReservationAuditLogRepository auditLogRepository;

    public OfferService(OfferRepository offerRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        ReservationAuditLogRepository auditLogRepository) {
        this.offerRepository = offerRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public OfferResponse createOffer(Long productId, OfferCreateRequest request, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new ConflictException("Product is currently " + product.getStatus() + " and cannot accept new offers.");
        }

        User buyer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer user not found"));

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new BadRequestException("You cannot make an offer on your own listed product.");
        }

        Offer offer = new Offer(product, buyer, request.getOfferedPrice(), request.getMessage());
        Offer saved = offerRepository.save(offer);

        return toOfferResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersForProduct(Long productId, UserPrincipal currentUser) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!product.getSeller().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view offers for this product.");
        }

        return offerRepository.findByProductId(productId).stream()
                .map(this::toOfferResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByBuyer(UserPrincipal currentUser) {
        return offerRepository.findByBuyerId(currentUser.getId()).stream()
                .map(this::toOfferResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersReceived(UserPrincipal currentUser) {
        return offerRepository.findByProductSellerId(currentUser.getId()).stream()
                .map(this::toOfferResponse)
                .toList();
    }

    @Transactional
    public OfferResponse acceptOffer(Long offerId, UserPrincipal currentUser) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        Long productId = offer.getProduct().getId();

        // Pessimistic Write Lock on product to prevent concurrent reservations or offer acceptance
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!product.getSeller().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new UnauthorizedException("Only the product seller can accept offers.");
        }

        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new ConflictException("Product is already " + product.getStatus() + " and cannot be reserved.");
        }

        // Accept this offer and reserve product
        offer.setStatus(OfferStatus.ACCEPTED);
        String prevStatus = product.getStatus().name();
        product.setStatus(ProductStatus.RESERVED);

        // Auto-reject other pending offers for this product
        List<Offer> allOffers = offerRepository.findByProductId(productId);
        for (Offer o : allOffers) {
            if (!o.getId().equals(offerId) && o.getStatus() == OfferStatus.PENDING) {
                o.setStatus(OfferStatus.REJECTED);
            }
        }

        // Record audit trail
        ReservationAuditLog auditLog = new ReservationAuditLog(
                product,
                offer.getBuyer(),
                prevStatus,
                ProductStatus.RESERVED.name(),
                "Accepted offer #" + offerId + " of ₹" + offer.getOfferedPrice()
        );
        auditLogRepository.save(auditLog);

        productRepository.save(product);
        Offer savedOffer = offerRepository.save(offer);

        return toOfferResponse(savedOffer);
    }

    @Transactional
    public OfferResponse rejectOffer(Long offerId, UserPrincipal currentUser) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        Product product = offer.getProduct();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!product.getSeller().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new UnauthorizedException("Only the product seller can reject offers.");
        }

        offer.setStatus(OfferStatus.REJECTED);
        Offer saved = offerRepository.save(offer);
        return toOfferResponse(saved);
    }

    private OfferResponse toOfferResponse(Offer offer) {
        OfferResponse res = new OfferResponse();
        res.setId(offer.getId());
        res.setProductId(offer.getProduct().getId());
        res.setProductTitle(offer.getProduct().getTitle());
        res.setProductPrice(offer.getProduct().getPrice());
        res.setBuyerId(offer.getBuyer().getId());
        res.setBuyerName(offer.getBuyer().getName());
        res.setBuyerEmail(offer.getBuyer().getEmail());
        res.setOfferedPrice(offer.getOfferedPrice());
        res.setMessage(offer.getMessage());
        res.setStatus(offer.getStatus().name());
        res.setCreatedAt(offer.getCreatedAt());
        return res;
    }
}
