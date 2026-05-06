package com.example.repairsystem.repository;

import com.example.repairsystem.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByBuildingIdOrderByNameAsc(Long buildingId);
    List<Section> findAllByOrderByBuildingNameAscNameAsc();
}
