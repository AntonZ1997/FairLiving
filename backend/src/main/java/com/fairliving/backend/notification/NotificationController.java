package com.fairliving.backend.notification;

import com.fairliving.backend.notification.dto.NotificationsReadRequest;
import com.fairliving.backend.user.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/households/{householdId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService, CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@PathVariable UUID householdId, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return notificationService.streamNotifications(householdId, userId);
    }

    @PostMapping("/read")
    public ResponseEntity<Void> readNotifications(@PathVariable UUID householdId,
                                                  @Valid @RequestBody NotificationsReadRequest request,
                                                  Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        notificationService.readNotifications(householdId, userId, request.notificationIds());
        return ResponseEntity.noContent().build();
    }
}
