package com.example.repairsystem.repository;

import com.example.repairsystem.model.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    List<RequestHistory> findByRequestIdOrderByChangedAtAsc(Long requestId);
}
