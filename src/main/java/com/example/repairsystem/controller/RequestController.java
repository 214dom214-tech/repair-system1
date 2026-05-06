package com.example.repairsystem.controller;

import com.example.repairsystem.model.Request;
import com.example.repairsystem.model.RequestPriority;
import com.example.repairsystem.model.ServiceType;
import com.example.repairsystem.model.RequestStatus;
import com.example.repairsystem.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Request createRequest(@RequestBody Request request) {
        return requestService.create(request);
    }

    @GetMapping
    public List<Request> getAllRequests() {
        return requestService.getAll();
    }

    @GetMapping("/by-status")
    public List<Request> getByStatus(@RequestParam RequestStatus status) {
        return requestService.getByStatus(status);
    }

    @GetMapping("/{id}")
    public Request getRequestById(@PathVariable Long id) {
        return requestService.getById(id);
    }

    @PatchMapping("/{id}/accept")
    public Request acceptRequest(@PathVariable Long id) {
        return requestService.accept(id);
    }

    /**
     * Закрыть заявку.
     * Body (необязательно): { "note": "...", "priority": "NORMAL"|"EMERGENCY" }
     */
    @PatchMapping("/{id}/close")
    public Request closeRequest(@PathVariable Long id,
                                @RequestBody(required = false) Map<String, String> body) {
        String note     = body != null ? body.get("note")     : null;
        String prioStr  = body != null ? body.get("priority") : null;
        String dtStr    = body != null ? body.get("downtimeMinutes") : null;
        RequestPriority priority       = prioStr != null ? RequestPriority.valueOf(prioStr) : null;
        Integer downtimeMinutes        = dtStr   != null && !dtStr.isBlank()
                                         ? Integer.parseInt(dtStr) : null;
        return requestService.close(id, note, priority, downtimeMinutes);
    }

    /** PATCH /api/requests/{id}/change-service — сменить службу */
    @PatchMapping("/{id}/change-service")
    public Request changeService(@PathVariable Long id,
                                  @RequestBody Map<String, String> body) {
        ServiceType svc = ServiceType.valueOf(body.getOrDefault("serviceType", "UNKNOWN"));
        return requestService.changeService(id, svc);
    }

    @PatchMapping("/{id}/reopen")
    public Request reopenRequest(@PathVariable Long id) {
        return requestService.reopen(id);
    }

    @PatchMapping("/{id}/confirm")
    public Request confirmRequest(@PathVariable Long id) {
        return requestService.confirm(id);
    }

    @PutMapping("/{id}")
    public Request updateRequest(@PathVariable Long id, @RequestBody Request updatedRequest) {
        return requestService.update(id, updatedRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRequest(@PathVariable Long id) {
        requestService.delete(id);
    }
}
