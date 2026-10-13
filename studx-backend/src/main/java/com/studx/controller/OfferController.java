package com.studx.controller;

import com.studx.dto.OfferCreateRequest;
import com.studx.dto.OfferResponse;
import com.studx.security.UserPrincipal;
import com.studx.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Offers & Inquiries", description = "Endpoints for buyer price offers and seller negotiation")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping("/products/{id}/offers")
    @Operation(summary = "Submit a price offer on an available product")
    public ResponseEntity<OfferResponse> createOffer(@PathVariable Long id,
                                                     @Valid @RequestBody OfferCreateRequest request,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        OfferResponse response = offerService.createOffer(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{id}/offers")
    @Operation(summary = "Get offers received for a specific product (Seller or Admin)")
    public ResponseEntity<List<OfferResponse>> getProductOffers(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        List<OfferResponse> offers = offerService.getOffersForProduct(id, currentUser);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/offers/sent")
    @Operation(summary = "Get offers submitted by current user")
    public ResponseEntity<List<OfferResponse>> getSentOffers(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(offerService.getOffersByBuyer(currentUser));
    }

    @GetMapping("/offers/received")
    @Operation(summary = "Get offers received on current user's listings")
    public ResponseEntity<List<OfferResponse>> getReceivedOffers(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(offerService.getOffersReceived(currentUser));
    }

    @PostMapping("/offers/{id}/accept")
    @Operation(summary = "Accept an offer and reserve the product (Concurrency protected)")
    public ResponseEntity<OfferResponse> acceptOffer(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        OfferResponse response = offerService.acceptOffer(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offers/{id}/reject")
    @Operation(summary = "Reject an offer (Seller or Admin)")
    public ResponseEntity<OfferResponse> rejectOffer(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserPrincipal currentUser) {
        OfferResponse response = offerService.rejectOffer(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
