package com.occazcar.service;

import com.occazcar.model.Notification;
import com.occazcar.model.User;
import com.occazcar.model.Vehicle;
import com.occazcar.repository.NotificationRepository;
import com.occazcar.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyNewVehicle(Vehicle vehicle) {
        try {
            logger.info("Création de notifications pour nouveau véhicule: {} {}",
                    vehicle.getBrand(), vehicle.getModel());

            // Notifier tous les acheteurs
            List<User> buyers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.UserRole.ACHETEUR)
                    .collect(Collectors.toList());

            logger.info("Nombre d'acheteurs trouvés: {}", buyers.size());

            for (User buyer : buyers) {
                try {
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
                    notification.setIsRead(false);

                    notificationRepository.save(notification);
                    logger.info("Notification créée pour l'acheteur: {}", buyer.getEmail());
                } catch (Exception e) {
                    logger.error("Erreur lors de la création de notification pour l'acheteur {}: {}",
                            buyer.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la notification du nouveau véhicule: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void notifyOfferStatusChange(Long buyerId, String vehicleInfo, String status) {
        try {
            logger.info("Création de notification pour changement de statut d'offre - buyerId: {}, status: {}",
                    buyerId, status);

            User buyer = userRepository.findById(buyerId).orElse(null);
            if (buyer == null) {
                logger.warn("Acheteur non trouvé avec l'ID: {}", buyerId);
                return;
            }

            Notification notification = new Notification();
            notification.setUser(buyer);
            notification.setIsRead(false);

            if ("ACCEPTEE".equals(status)) {
                notification.setTitle("Offre acceptée !");
                notification.setMessage("Votre offre pour " + vehicleInfo + " a été acceptée");
                notification.setType("OFFER_ACCEPTED");
            } else if ("REFUSEE".equals(status)) {
                notification.setTitle("Offre refusée");
                notification.setMessage("Votre offre pour " + vehicleInfo + " a été refusée");
                notification.setType("OFFER_REFUSED");
            } else {
                logger.warn("Statut d'offre non reconnu: {}", status);
                return;
            }

            notificationRepository.save(notification);
            logger.info("Notification de changement de statut créée pour l'acheteur: {}", buyer.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de la notification du changement de statut: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        try {
            logger.info("Récupération des notifications pour l'utilisateur: {}", userId);
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            logger.info("Nombre de notifications trouvées: {}", notifications.size());
            return notifications;
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications pour l'utilisateur {}: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors du chargement des notifications", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        try {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications non lues: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du chargement des notifications non lues", e);
        }
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        try {
            Long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
            logger.debug("Nombre de notifications non lues pour l'utilisateur {}: {}", userId, count);
            return count;
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des notifications non lues: {}", e.getMessage());
            return 0L;
        }
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

            if (!notification.getUser().getId().equals(userId)) {
                throw new RuntimeException("Non autorisé");
            }

            notification.setIsRead(true);
            notificationRepository.save(notification);
            logger.info("Notification {} marquée comme lue", notificationId);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de la notification comme lue: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        try {
            List<Notification> notifications =
                    notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

            for (Notification notification : notifications) {
                notification.setIsRead(true);
            }
            notificationRepository.saveAll(notifications);
            logger.info("Toutes les notifications marquées comme lues pour l'utilisateur: {}", userId);
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de toutes les notifications comme lues: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du marquage des notifications", e);
        }
    }
}