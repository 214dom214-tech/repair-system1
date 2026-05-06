package com.example.repairsystem.repository;

import com.example.repairsystem.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByNameContainingIgnoreCaseOrInventoryNumberContainingIgnoreCase(String name, String inv);
    Optional<Equipment> findByInventoryNumber(String inventoryNumber);
}
