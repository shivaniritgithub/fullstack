package com.internal.tasktracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Normalize query input
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase(Locale.ROOT) + "%";

        // Parse status filter: invalid value -> 400 instead of a 500 crash
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid status. Allowed: OPEN, IN_PROGRESS, DONE"));
            }
        }

        // Keep paging values in a safe range
        int safePage = Math.max(1, page);
        int safePageSize = Math.min(Math.max(1, pageSize), 100);

        System.out.println("[TaskController] q=\"" + query + "\" status=" + normalizedStatus
                + " page=" + safePage + " pageSize=" + safePageSize);

        List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);

        // (long) avoids integer overflow; min() keeps start inside the list
        int start = (int) Math.min((long) (safePage - 1) * safePageSize, allResults.size());
        int end = Math.min(start + safePageSize, allResults.size());
        List<Task> pageResults = allResults.subList(start, end);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", pageResults);
        response.put("total", allResults.size());
        response.put("page", safePage);
        response.put("pageSize", safePageSize);

        return ResponseEntity.ok(response);
    }
}