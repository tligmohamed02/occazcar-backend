package com.occazcar.service;

import com.occazcar.model.Notification;
import com.occazcar.model.User;
import com.occazcar.model.Vehicle;
import com.occazcar.repository.NotificationRepository;
import com.occazcar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyNewVehicle(Vehicle vehicle) {
        // Notifier tous les acheteurs
        List<User> buyers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.ACHETEUR)
                .collect(Collectors.toList());

        for (User buyer : buyers) {
            Notification notification = new Notification();
            notification.setUser(buyer);
            notification.setTitle("Nouveau véhicule disponible");
            notification.setMessage(String.format(
                    "Un %s %s de %d vient d'être ajouté à %.0f TND",
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getYear(),
                    vehicle.getPrice()
            ));
            notification.setType("NEW_VEHICLE");
            notification.setRelatedEntityId(vehicle.getId());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyOfferStatusChange(Long buyerId, String vehicleInfo, String status) {
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (buyer == null) return;

        Notification notification = new Notification();
        notification.setUser(buyer);

        if ("ACCEPTEE".equals(status)) {
            notification.setTitle("Offre acceptée !");
            notification.setMessage("Votre offre pour " + vehicleInfo + " a été acceptée");
            notification.setType("OFFER_ACCEPTED");
        } else if ("REFUSEE".equals(status)) {
            notification.setTitle("Offre refusée");
            notification.setMessage("Votre offre pour " + vehicleInfo + " a été refusée");
            notification.setType("OFFER_REFUSED");
        }

        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}