package com.occazcar.controller;

import com.occazcar.dto.OfferRequest;
import com.occazcar.dto.OfferResponse;
import com.occazcar.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public ResponseEntity<?> createOffer(@Valid @RequestBody OfferRequest request,
                                         Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            OfferResponse response = offerService.createOffer(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buyer")
    public ResponseEntity<List<OfferResponse>> getBuyerOffers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<OfferResponse> offers = offerService.getBuyerOffers(userId);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OfferResponse>> getSellerOffers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<OfferResponse> offers = offerService.getSellerOffers(userId);
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getVehicleOffers(@PathVariable Long vehicleId,
                                              Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<OfferResponse> offers = offerService.getVehicleOffers(vehicleId, userId);
            return ResponseEntity.ok(offers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOfferStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            String status = body.get("status");
            OfferResponse response = offerService.updateOfferStatus(id, status, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}