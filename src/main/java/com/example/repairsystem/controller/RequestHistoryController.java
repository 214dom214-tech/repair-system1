package com.example.repairsystem.controller;

import com.example.repairsystem.model.RequestHistory;
import com.example.repairsystem.repository.RequestHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}/history")
public class RequestHistoryController {

    @Autowired
    private RequestHistoryRepository historyRepository;

    @GetMapping
    public List<RequestHistory> getHistory(@PathVariable Long requestId) {
        return historyRepository.findByRequestIdOrderByChangedAtAsc(requestId);
    }
}
