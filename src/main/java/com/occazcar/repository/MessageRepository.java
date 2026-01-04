package com.occazcar.repository;

import com.occazcar.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByVehicleIdOrderByCreatedAtAsc(Long vehicleId);

    @Query("SELECT m FROM Message m WHERE (m.sender.id = ?1 AND m.receiver.id = ?2 AND m.vehicle.id = ?3) " +
            "OR (m.sender.id = ?2 AND m.receiver.id = ?1 AND m.vehicle.id = ?3) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(Long userId1, Long userId2, Long vehicleId);

    @Query("SELECT DISTINCT m.vehicle.id FROM Message m WHERE m.sender.id = ?1 OR m.receiver.id = ?1")
    List<Long> findVehicleIdsWithMessages(Long userId);

    Long countBySenderIdAndIsReadFalse(Long receiverId);
}