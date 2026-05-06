package com.example.repairsystem.controller;

import com.example.repairsystem.model.Building;
import com.example.repairsystem.model.Section;
import com.example.repairsystem.repository.BuildingRepository;
import com.example.repairsystem.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

    @Autowired BuildingRepository buildingRepo;
    @Autowired SectionRepository sectionRepo;

    @GetMapping
    public List<Building> getAll() { return buildingRepo.findAllByOrderByNameAsc(); }

    @GetMapping("/{id}")
    public Building getById(@PathVariable Long id) {
        return buildingRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Корпус не найден"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Building create(@RequestBody Building b) { return buildingRepo.save(b); }

    @PutMapping("/{id}")
    public Building update(@PathVariable Long id, @RequestBody Building b) {
        Building existing = buildingRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Корпус не найден"));
        existing.setName(b.getName());
        existing.setDescription(b.getDescription());
        return buildingRepo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!buildingRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Корпус не найден");
        buildingRepo.deleteById(id);
    }

    // Участки конкретного корпуса
    @GetMapping("/{id}/sections")
    public List<Section> getSections(@PathVariable Long id) {
        return sectionRepo.findByBuildingIdOrderByNameAsc(id);
    }

    // Создать участок в корпусе
    @PostMapping("/{id}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public Section createSection(@PathVariable Long id, @RequestBody Section s) {
        Building building = buildingRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Корпус не найден"));
        s.setBuilding(building);
        return sectionRepo.save(s);
    }
}
