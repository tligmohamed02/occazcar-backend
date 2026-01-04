package com.occazcar.service;

import com.occazcar.dto.MessageRequest;
import com.occazcar.dto.MessageResponse;
import com.occazcar.dto.ConversationResponse;
import com.occazcar.model.Message;
import com.occazcar.model.User;
import com.occazcar.model.Vehicle;
import com.occazcar.repository.MessageRepository;
import com.occazcar.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final VehicleRepository vehicleRepository;
    private final AuthService authService;

    public MessageService(MessageRepository messageRepository,
                          VehicleRepository vehicleRepository,
                          AuthService authService) {
        this.messageRepository = messageRepository;
        this.vehicleRepository = vehicleRepository;
        this.authService = authService;
    }

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Long senderId) {
        User sender = authService.getUserById(senderId);
        User receiver = authService.getUserById(request.getReceiverId());
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        Message message = new Message();
        message.setVehicle(vehicle);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());

        message = messageRepository.save(message);

        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(Long vehicleId, Long otherUserId, Long currentUserId) {
        return messageRepository.findConversation(currentUserId, otherUserId, vehicleId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long userId) {
        List<Long> vehicleIds = messageRepository.findVehicleIdsWithMessages(userId);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Long vehicleId : vehicleIds) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
            if (vehicle == null) continue;

            List<Message> messages = messageRepository.findByVehicleIdOrderByCreatedAtAsc(vehicleId);
            if (messages.isEmpty()) continue;

            Message lastMessage = messages.get(messages.size() - 1);

            // Déterminer l'autre utilisateur
            Long otherUserId = lastMessage.getSender().getId().equals(userId)
                    ? lastMessage.getReceiver().getId()
                    : lastMessage.getSender().getId();

            User otherUser = authService.getUserById(otherUserId);

            // Compter les messages non lus
            int unreadCount = (int) messages.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && !m.getIsRead())
                    .count();

            ConversationResponse conv = new ConversationResponse();
            conv.setVehicleId(vehicleId);
            conv.setVehicleBrand(vehicle.getBrand());
            conv.setVehicleModel(vehicle.getModel());
            conv.setOtherUserId(otherUserId);
            conv.setOtherUserName(otherUser.getFullName());
            conv.setLastMessage(lastMessage.getContent());
            conv.setLastMessageTime(lastMessage.getCreatedAt().toString());
            conv.setUnreadCount(unreadCount);

            conversations.add(conv);
        }

        return conversations;
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (message.getReceiver().getId().equals(userId)) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    private MessageResponse mapToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setVehicleId(message.getVehicle().getId());
        response.setVehicleBrand(message.getVehicle().getBrand());
        response.setVehicleModel(message.getVehicle().getModel());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setReceiverId(message.getReceiver().getId());
        response.setReceiverName(message.getReceiver().getFullName());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt().toString());
        return response;
    }
}