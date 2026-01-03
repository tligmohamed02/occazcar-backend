package com.occazcar.repository;

import com.occazcar.model.Vehicle;
import com.occazcar.model.Vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findBySellerIdAndStatus(Long sellerId, VehicleStatus status);
    List<Vehicle> findByStatus(VehicleStatus status);
}