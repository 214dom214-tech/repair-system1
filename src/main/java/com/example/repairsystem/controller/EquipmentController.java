package com.example.repairsystem.controller;

import com.example.repairsystem.model.Equipment;
import com.example.repairsystem.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/equipment", produces = "application/json; charset=UTF-8")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    /** POST /api/equipment — создать одно оборудование */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipment createEquipment(@RequestBody Equipment equipment) {
        return equipmentService.create(equipment);
    }

    /** POST /api/equipment/batch — создать несколько */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Equipment> createBatch(@RequestBody List<Equipment> list) {
        return equipmentService.createBatch(list);
    }

    @GetMapping
    public List<Equipment> getAllEquipment() { return equipmentService.getAll(); }

    @GetMapping("/search")
    public List<Equipment> search(@RequestParam String q) { return equipmentService.search(q); }

    @GetMapping("/{id}")
    public Equipment getEquipmentById(@PathVariable Long id) { return equipmentService.getById(id); }

    @PutMapping("/{id}")
    public Equipment updateEquipment(@PathVariable Long id, @RequestBody Equipment updated) {
        return equipmentService.update(id, updated);
    }

    /**
     * PATCH /api/equipment/{id}/retire — списать оборудование.
     * Не удаляет запись: история заявок сохраняется, инв.номер остаётся в БД.
     * При повторном импорте того же инв.номера — флаг снимается.
     */
    @PatchMapping("/{id}/retire")
    public Equipment retireEquipment(@PathVariable Long id) {
        return equipmentService.retire(id);
    }

    /**
     * PATCH /api/equipment/{id}/restore — восстановить списанное оборудование.
     */
    @PatchMapping("/{id}/restore")
    public Equipment restoreEquipment(@PathVariable Long id) {
        return equipmentService.restore(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipment(@PathVariable Long id) { equipmentService.delete(id); }
}
