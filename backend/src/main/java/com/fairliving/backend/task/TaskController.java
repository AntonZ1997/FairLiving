package com.fairliving.backend.task;

import com.fairliving.backend.task.dto.AssignedTaskResponse;
import com.fairliving.backend.task.dto.CreateTaskRequest;
import com.fairliving.backend.task.dto.DifficultyResponse;
import com.fairliving.backend.task.dto.TaskCompletionResponse;
import com.fairliving.backend.user.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskController {
    private final TaskService taskService;
    private final CurrentUserProvider currentUserProvider;

    public TaskController(TaskService taskService, CurrentUserProvider currentUserProvider) {
        this.taskService = taskService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/difficulties")
    public ResponseEntity<List<DifficultyResponse>> getDifficulties() {
        return ResponseEntity.ok(taskService.getDifficulties());
    }

    @GetMapping("/households/{householdId}/tasks/open")
    public ResponseEntity<List<AssignedTaskResponse>> getOpenTasks(@PathVariable UUID householdId, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(taskService.getOpenTasksForUser(householdId, userId));
    }

    @PostMapping("/households/{householdId}/tasks")
    public ResponseEntity<AssignedTaskResponse> createTask(@PathVariable UUID householdId, @Valid @RequestBody CreateTaskRequest request, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(householdId, userId, request));
    }

    @PostMapping("/assigned-tasks/{assignedTaskId}/complete")
    public ResponseEntity<TaskCompletionResponse> completeTask(@PathVariable UUID assignedTaskId, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(taskService.completeTask(assignedTaskId, userId));
    }
}
