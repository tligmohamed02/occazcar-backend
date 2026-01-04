package com.occazcar.controller;

import com.occazcar.dto.ConversationResponse;
import com.occazcar.dto.MessageRequest;
import com.occazcar.dto.MessageResponse;
import com.occazcar.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageRequest request,
                                         Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            MessageResponse response = messageService.sendMessage(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(
            @RequestParam Long vehicleId,
            @RequestParam Long otherUserId,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            List<MessageResponse> messages = messageService.getConversation(
                    vehicleId, otherUserId, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-conversations")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<ConversationResponse> conversations = messageService.getMyConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            messageService.markAsRead(id, userId);
            return ResponseEntity.ok("Message marqué comme lu");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}