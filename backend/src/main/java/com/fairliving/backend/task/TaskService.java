package com.fairliving.backend.task;

import com.fairliving.backend.exception.*;
import com.fairliving.backend.gamification.GamificationService;
import com.fairliving.backend.gamification.dto.CompletionReward;
import com.fairliving.backend.household.HouseholdMemberRepository;
import com.fairliving.backend.household.HouseholdService;
import com.fairliving.backend.task.dto.*;
import de.fairliving.backend.jooq.tables.records.AssignedTaskRecord;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import de.fairliving.backend.jooq.tables.records.TaskRecord;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AssignedTaskRepository assignedTaskRepository;
    private final DifficultyRepository difficultyRepository;
    private final TaskDistributionService taskDistributionService;
    private final HouseholdService householdService;
    private final HouseholdMemberRepository householdMemberRepository;
    private final GamificationService gamificationService;

    public TaskService(TaskRepository taskRepository, AssignedTaskRepository assignedTaskRepository, DifficultyRepository difficultyRepository, TaskDistributionService taskDistributionService, HouseholdService householdService, HouseholdMemberRepository householdMemberRepository, GamificationService gamificationService) {
        this.taskRepository = taskRepository;
        this.assignedTaskRepository = assignedTaskRepository;
        this.difficultyRepository = difficultyRepository;
        this.taskDistributionService = taskDistributionService;
        this.householdService = householdService;
        this.householdMemberRepository = householdMemberRepository;
        this.gamificationService = gamificationService;
    }

    public List<DifficultyResponse> getDifficulties() {
        return difficultyRepository.findAll();
    }

    public List<AssignedTaskResponse> getOpenTasksForUser(UUID householdId, UUID userId) {
        HouseholdMemberRecord householdMember = householdService.requireMembership(householdId, userId);
        return assignedTaskRepository.findOpenTasksByMemberId(householdMember.getId());
    }

    @Transactional
    public AssignedTaskResponse createTask(UUID householdId, UUID userId, CreateTaskRequest createTaskRequest) {
        householdService.requireMembership(householdId, userId);

        if(!difficultyRepository.exists(createTaskRequest.difficultyId())) {
            throw new DifficultyNotFoundException(createTaskRequest.difficultyId());
        }

        TaskRecord task = taskRepository.insert(
                householdId,
                createTaskRequest.name(),
                createTaskRequest.description(),
                createTaskRequest.difficultyId(),
                createTaskRequest.intervalDays()
        );

        UUID householdMemberId = taskDistributionService.determineHouseholdMemberForTask(householdId);
        AssignedTaskRecord assignedTask = assignedTaskRepository.insert(
                task.getId(),
                householdMemberId,
                createTaskRequest.dueDate()
        );

        return assignedTaskRepository.findById(assignedTask.getId()).orElseThrow();
    }

    @Transactional
    public TaskCompletionResponse completeTask(UUID assignedTaskId, UUID userId) {
        AssignedTaskRecord assignedTask = assignedTaskRepository.findRecordById(assignedTaskId)
                .orElseThrow(() -> new AssignedTaskNotFoundException(assignedTaskId));

        if(TaskStatus.COMPLETED.getStatus().equals(assignedTask.getStatus())) {
            throw new TaskAlreadyCompletedException(assignedTaskId);
        }

        HouseholdMemberRecord householdMember = householdMemberRepository.findById(assignedTask.getAssignedMemberId())
                .orElseThrow(() -> new AssignedTaskNotFoundException(assignedTaskId));

        if(!householdMember.getUserId().equals(userId)) {
            throw new TaskIsNotAssignedToUserException(assignedTaskId, userId);
        }

        TaskRecord task = taskRepository.findById(assignedTask.getTaskId())
                .orElseThrow(() -> new AssignedTaskNotFoundException(assignedTaskId));

        Instant completedAt = Instant.now();
        boolean completedOnTime = !completedAt.isAfter(assignedTask.getDueDate());

        assignedTaskRepository.complete(assignedTaskId, completedAt);

        int baseXp = difficultyRepository.findBaseXp(task.getDifficultyId());
        CompletionReward completionReward = gamificationService.calculateCompletionReward(householdMember, baseXp, completedOnTime);

        if(task.getIntervalDays() > 0) {
            Instant newDueDate = completedAt.plus(task.getIntervalDays(), ChronoUnit.DAYS);
            UUID nextAssignedHouseholdMemberId = taskDistributionService.determineHouseholdMemberForTask(householdMember.getHouseholdId());
            assignedTaskRepository.insert(task.getId(), nextAssignedHouseholdMemberId, newDueDate);
        }

        return new TaskCompletionResponse(
                completionReward.earnedExperiencePoints(),
                completionReward.totalExperiencePoints(),
                completionReward.streakCount(),
                completionReward.level().getLevelNumber(),
                completionReward.level().getTitle(),
                completionReward.leveledUp(),
                completedOnTime
        );
    }

    public TaskDetailResponse getTaskDetails(UUID taskId, UUID userId) {
        TaskRecord task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        HouseholdMemberRecord householdMember = householdService.requireMembership(task.getHouseholdId(), userId);

        return taskRepository.fetchTaskDetails(taskId, householdMember.getId())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Transactional
    public TaskDetailResponse deactivateTask(UUID taskId, UUID userId) {
        TaskRecord task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        HouseholdMemberRecord householdMember = householdService.requireMembership(task.getHouseholdId(), userId);

        taskRepository.setActive(taskId, false);

        assignedTaskRepository.deleteUncompletedAssignedTaskByTaskId(taskId);

        return taskRepository.fetchTaskDetails(taskId, householdMember.getId()).orElseThrow();
    }

    @Transactional
    public TaskDetailResponse activateTask(UUID taskId, UUID userId, Instant dueDate) {
        TaskRecord task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        HouseholdMemberRecord householdMember = householdService.requireMembership(task.getHouseholdId(), userId);

        taskRepository.setActive(taskId, true);

        UUID assignedMemberId = taskDistributionService.determineHouseholdMemberForTask(task.getHouseholdId());
        assignedTaskRepository.insert(task.getId(), assignedMemberId, dueDate);

        return taskRepository.fetchTaskDetails(taskId, householdMember.getId()).orElseThrow();
    }
}
