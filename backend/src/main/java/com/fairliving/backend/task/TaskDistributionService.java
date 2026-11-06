package com.fairliving.backend.task;

import com.fairliving.backend.task.dto.MemberWorkload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Aufgabenverteilungs-Service, der Arbeitsaufgaben eines Haushalts dynamisch an Haushaltsmitglieder mit der geringsten
 * Workload verteilt, wenn das Mitglied nicht bereits 3 offene Aufgaben mehr hat als das Mitglied mit den geringsten
 * offenen Aufgaben
 */
@Service
public class TaskDistributionService {

    private static final int WORKLOAD_WINDOW_DAYS = 30; // Zeitraum in Tagen, für den die Workload berechnet wird
    private static final int MAX_OPEN_TASK_LEAD = 3;

    private final AssignedTaskRepository assignedTaskRepository;

    public TaskDistributionService(AssignedTaskRepository assignedTaskRepository) {
        this.assignedTaskRepository = assignedTaskRepository;
    }

    public UUID determineHouseholdMemberForTask(UUID householdId) {
        return determineHouseholdMemberForTask(householdId, null);
    }

    /**
     * Ermittelt das Haushaltsmitglied, das die nächste Aufgabe zugewiesen bekommt.
     * @param householdId ID des Haushalts
     * @param excludedMemberId Mitglied, das in der Emittlung nicht berücksichtigt wird, zum Beispiel wenn es den Haushalt verlässt
     *                         und seine Aufgaben auf die anderen Mitglieder verteilt werden.
     * @return ID des ermittelten Haushaltsmitglieds
     */
    public UUID determineHouseholdMemberForTask(UUID householdId, UUID excludedMemberId) {
        Instant completedSince = Instant.now().minus(WORKLOAD_WINDOW_DAYS, ChronoUnit.DAYS);
        List<MemberWorkload> workloads = assignedTaskRepository.findWorkloads(householdId, completedSince)
                .stream()
                .filter(member -> !member.memberId().equals(excludedMemberId))
                .toList();

        if (workloads.isEmpty()) {
            throw new IllegalArgumentException("No workloads found for household: " + householdId);
        }

        int fewestOpenTasks = workloads.stream()
                .mapToInt(MemberWorkload::openTaskCount)
                .min()
                .orElseThrow();

        return workloads.stream()
                .sorted(Comparator.comparingDouble(MemberWorkload::workload))
                // Die Liste ist aufsteigend nach der Workload sortiert und wird gefiltert nach Mitgliedern, die
                // noch keinen Vorsprung von drei offenen Aufgaben haben
                .filter(member -> member.openTaskCount() - fewestOpenTasks < MAX_OPEN_TASK_LEAD)
                .findFirst()
                .orElseThrow()
                .memberId();
    }


}
