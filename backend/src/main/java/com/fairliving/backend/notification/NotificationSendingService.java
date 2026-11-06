package com.fairliving.backend.notification;

import com.fairliving.backend.notification.dto.NotificationCreatedEvent;
import com.fairliving.backend.notification.dto.NotificationResponse;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSendingService {

    private static final long STREAM_TIMEOUT = Duration.ofMinutes(10).toMillis();
    private static final String NOTIFICATION_EVENT = "notification";

    private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final NotificationRepository notificationRepository;

    public NotificationSendingService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public SseEmitter subscribe(UUID householdMemberId, List<NotificationResponse> notifications) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT);

        emitters.computeIfAbsent(householdMemberId, _ -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(householdMemberId, emitter));
        emitter.onTimeout(() -> removeEmitter(householdMemberId, emitter));
        emitter.onError(_ -> removeEmitter(householdMemberId, emitter));

        sendNotifications(householdMemberId, emitter, notifications);

        return emitter;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        for (UUID householdMemberId : event.householdMemberIds()) {
            Set<SseEmitter> set = emitters.get(householdMemberId);

            if (set == null || set.isEmpty()) {
                continue;
            }

            List<NotificationResponse> notifications = notificationRepository.findNewNotificationsByHouseholdMember(householdMemberId);
            for (SseEmitter emitter : set) {
                sendNotifications(householdMemberId, emitter, notifications);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    public void pingHouseholdMembers() {
        emitters.forEach((householdMemberId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException e) {
                    removeEmitter(householdMemberId, emitter);
                }
            }
        });
    }

    private void sendNotifications(UUID householdMemberId, SseEmitter emitter, List<NotificationResponse> notifications) {
        try {
            emitter.send(SseEmitter.event().name(NOTIFICATION_EVENT).data(notifications, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            removeEmitter(householdMemberId, emitter);
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(UUID householdMemberId, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(householdMemberId);
        if (set == null) {
            return;
        }

        set.remove(emitter);

        if (set.isEmpty()) {
            emitters.remove(householdMemberId);
        }
    }

}
