package com.fairliving.backend.task;

import com.fairliving.backend.task.dto.MemberWorkload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TaskDistributionService {

    private static final int WORKLOAD_WINDOW_DAYS = 30;
    private static final int MAX_OPEN_TASK_LEAD = 3;

    private final AssignedTaskRepository assignedTaskRepository;

    public TaskDistributionService(AssignedTaskRepository assignedTaskRepository) {
        this.assignedTaskRepository = assignedTaskRepository;
    }

    public UUID determineHouseholdMemberForTask(UUID householdId) {
        return determineHouseholdMemberForTask(householdId, null);
    }

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
                .filter(member -> member.openTaskCount() - fewestOpenTasks < MAX_OPEN_TASK_LEAD)
                .findFirst()
                .orElseThrow()
                .memberId();
    }


}
