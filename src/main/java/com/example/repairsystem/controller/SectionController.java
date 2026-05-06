package com.example.repairsystem.controller;

import com.example.repairsystem.model.Section;
import com.example.repairsystem.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired SectionRepository sectionRepo;

    @GetMapping
    public List<Section> getAll() { return sectionRepo.findAllByOrderByBuildingNameAscNameAsc(); }

    @PutMapping("/{id}")
    public Section update(@PathVariable Long id, @RequestBody Section s) {
        Section existing = sectionRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Участок не найден"));
        existing.setName(s.getName());
        existing.setDescription(s.getDescription());
        return sectionRepo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!sectionRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Участок не найден");
        sectionRepo.deleteById(id);
    }
}
