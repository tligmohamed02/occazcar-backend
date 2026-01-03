package com.occazcar.repository;

import com.occazcar.model.Offer;
import com.occazcar.model.Offer.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByVehicleId(Long vehicleId);
    List<Offer> findByBuyerId(Long buyerId);
    List<Offer> findByVehicleSellerId(Long sellerId);
    List<Offer> findByBuyerIdAndStatus(Long buyerId, OfferStatus status);
}