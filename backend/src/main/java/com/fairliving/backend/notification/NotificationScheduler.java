package com.fairliving.backend.notification;

import com.fairliving.backend.notification.dto.TaskDueNotice;
import com.fairliving.backend.task.AssignedTaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Erstellt Fälligkeitsbenachrichtigungen um 7 Uhr morgens jedes Tages.
 * Dabei gibt es eine Warnung für Aufgaben, die innerhalb der nächsten X Stunden {@code DUE_IN_X_HOURS_NOTIFICATION}
 * (24 Stunden) fällig sind und einen Alarm für überfällige Aufgaben.
 */
@Component
public class NotificationScheduler {

    private static final int DUE_IN_X_HOURS_NOTIFICATION = 24;

    private final AssignedTaskRepository assignedTaskRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(AssignedTaskRepository assignedTaskRepository, NotificationService notificationService) {
        this.assignedTaskRepository = assignedTaskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void scheduledNotification() {
        Instant now = Instant.now();
        Instant time = now.plus(DUE_IN_X_HOURS_NOTIFICATION, ChronoUnit.HOURS);

        List<TaskDueNotice> openTasksDueBefore = assignedTaskRepository.findOpenTasksDueBefore(time);
        for (TaskDueNotice taskDueNotice : openTasksDueBefore) {
            if (taskDueNotice.dueDate().isBefore(now)) {
                notificationService.notifyOverdueTask(taskDueNotice.householdMemberId(), taskDueNotice.taskName());
            } else {
                notificationService.notifyDueTodayTask(taskDueNotice.householdMemberId(), taskDueNotice.taskName());
            }
        }
    }
}
