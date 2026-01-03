package com.occazcar.repository;

import com.occazcar.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserIdAndActiveTrue(Long userId);
    List<Alert> findByActiveTrue();
}