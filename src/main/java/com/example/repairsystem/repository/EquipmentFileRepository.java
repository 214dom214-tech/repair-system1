package com.example.repairsystem.repository;

import com.example.repairsystem.model.EquipmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentFileRepository extends JpaRepository<EquipmentFile, Long> {
    List<EquipmentFile> findByEquipmentId(Long equipmentId);
}
