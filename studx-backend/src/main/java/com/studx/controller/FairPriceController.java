package com.studx.controller;

import com.studx.dto.FairPriceResponse;
import com.studx.service.FairPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Fair Price Insights", description = "Endpoints for student second-hand market pricing intelligence")
public class FairPriceController {

    private final FairPriceService fairPriceService;

    public FairPriceController(FairPriceService fairPriceService) {
        this.fairPriceService = fairPriceService;
    }

    @GetMapping("/price-insights")
    @Operation(summary = "Calculate fair price insights based on historical campus listings")
    public ResponseEntity<FairPriceResponse> getPriceInsights(
            @RequestParam Long categoryId,
            @RequestParam(required = false) String condition) {
        FairPriceResponse response = fairPriceService.getFairPriceInsights(categoryId, condition);
        return ResponseEntity.ok(response);
    }
}
