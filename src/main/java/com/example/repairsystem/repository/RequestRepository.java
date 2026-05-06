package com.example.repairsystem.repository;

import com.example.repairsystem.model.Request;
import com.example.repairsystem.model.RequestPriority;
import com.example.repairsystem.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByStatus(RequestStatus status);

    List<Request> findByPriority(RequestPriority priority);

    List<Request> findByStatusAndPriority(RequestStatus status, RequestPriority priority);

    List<Request> findByEquipmentId(Long equipmentId);

    List<Request> findAllByOrderByCreatedAtDesc();
}
