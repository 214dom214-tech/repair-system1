package com.example.repairsystem.repository;

import com.example.repairsystem.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findAllByOrderByNameAsc();
}
