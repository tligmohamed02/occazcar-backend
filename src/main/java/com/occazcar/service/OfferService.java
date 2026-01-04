package com.occazcar.service;

import com.occazcar.dto.OfferRequest;
import com.occazcar.dto.OfferResponse;
import com.occazcar.model.Offer;
import com.occazcar.model.User;
import com.occazcar.model.Vehicle;
import com.occazcar.repository.OfferRepository;
import com.occazcar.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final VehicleRepository vehicleRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public OfferService(OfferRepository offerRepository,
                        VehicleRepository vehicleRepository,
                        AuthService authService,
                        NotificationService notificationService) {
        this.offerRepository = offerRepository;
        this.vehicleRepository = vehicleRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long buyerId) {
        User buyer = authService.getUserById(buyerId);
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        Offer offer = new Offer();
        offer.setVehicle(vehicle);
        offer.setBuyer(buyer);
        offer.setProposedPrice(request.getProposedPrice());
        offer.setMessage(request.getMessage());

        offer = offerRepository.save(offer);

        return mapToResponse(offer);
    }

    public List<OfferResponse> getBuyerOffers(Long buyerId) {
        return offerRepository.findByBuyerId(buyerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OfferResponse> getSellerOffers(Long sellerId) {
        return offerRepository.findByVehicleSellerId(sellerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OfferResponse> getVehicleOffers(Long vehicleId, Long sellerId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (!vehicle.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Non autorisé");
        }

        return offerRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OfferResponse updateOfferStatus(Long offerId, String status, Long userId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (!offer.getVehicle().getSeller().getId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        offer.setStatus(Offer.OfferStatus.valueOf(status.toUpperCase()));
        offer = offerRepository.save(offer);

        // Si l'offre est acceptée, marquer le véhicule comme VENDU
        if ("ACCEPTEE".equals(status)) {
            Vehicle vehicle = offer.getVehicle();
            vehicle.setStatus(Vehicle.VehicleStatus.VENDU);
            vehicleRepository.save(vehicle);
        }

        // Notifier l'acheteur du changement de statut
        String vehicleInfo = offer.getVehicle().getBrand() + " " + offer.getVehicle().getModel();
        notificationService.notifyOfferStatusChange(
                offer.getBuyer().getId(),
                vehicleInfo,
                status
        );

        return mapToResponse(offer);
    }

    private OfferResponse mapToResponse(Offer offer) {
        OfferResponse response = new OfferResponse();
        response.setId(offer.getId());
        response.setVehicleId(offer.getVehicle().getId());
        response.setVehicleBrand(offer.getVehicle().getBrand());
        response.setVehicleModel(offer.getVehicle().getModel());
        response.setBuyerId(offer.getBuyer().getId());
        response.setBuyerName(offer.getBuyer().getFullName());
        response.setBuyerPhone(offer.getBuyer().getPhone());
        response.setProposedPrice(offer.getProposedPrice());
        response.setMessage(offer.getMessage());
        response.setStatus(offer.getStatus().name());
        response.setCreatedAt(offer.getCreatedAt().toString());
        return response;
    }
}