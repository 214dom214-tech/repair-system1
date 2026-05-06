package com.example.repairsystem.service;

import com.example.repairsystem.model.Equipment;
import com.example.repairsystem.model.Section;
import com.example.repairsystem.repository.EquipmentRepository;
import com.example.repairsystem.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class EquipmentService {

    @Autowired EquipmentRepository equipmentRepository;
    @Autowired SectionRepository sectionRepository;

    public List<Equipment> getAll() { return equipmentRepository.findAll(); }

    public List<Equipment> search(String query) {
        return equipmentRepository.findByNameContainingIgnoreCaseOrInventoryNumberContainingIgnoreCase(query, query);
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Оборудование с id=" + id + " не найдено"));
    }

    public Equipment create(Equipment e) {
        resolveSection(e);
        if (e.getInventoryNumber() != null && !e.getInventoryNumber().isBlank()) {
            equipmentRepository.findByInventoryNumber(e.getInventoryNumber())
                .ifPresent(ex -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Инвентарный номер '" + e.getInventoryNumber() + "' уже занят"); });
        }
        return equipmentRepository.save(e);
    }

    public List<Equipment> createBatch(List<Equipment> list) {
        list.forEach(this::resolveSection);
        return equipmentRepository.saveAll(list);
    }

    public Equipment update(Long id, Equipment updated) {
        Equipment existing = getById(id);
        existing.setName(updated.getName());
        existing.setInventoryNumber(updated.getInventoryNumber());
        existing.setResponsiblePerson(updated.getResponsiblePerson());
        existing.setManufacturer(updated.getManufacturer());
        existing.setModel(updated.getModel());
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setNote(updated.getNote());
        // Обновляем участок
        if (updated.getSection() != null && updated.getSection().getId() != null) {
            Section s = sectionRepository.findById(updated.getSection().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Участок не найден"));
            existing.setSection(s);
        } else {
            existing.setSection(null);
        }
        return equipmentRepository.save(existing);
    }

    /** Списать оборудование — не удаляет, только помечает флагом */
    public Equipment retire(Long id) {
        Equipment eq = getById(id);
        eq.setRetired(true);
        eq.setRetiredDate(java.time.LocalDate.now());
        return equipmentRepository.save(eq);
    }

    /** Восстановить списанное оборудование */
    public Equipment restore(Long id) {
        Equipment eq = getById(id);
        eq.setRetired(false);
        eq.setRetiredDate(null);
        return equipmentRepository.save(eq);
    }

    public void delete(Long id) {
        if (!equipmentRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Оборудование с id=" + id + " не найдено");
        equipmentRepository.deleteById(id);
    }

    private void resolveSection(Equipment e) {
        if (e.getSection() != null && e.getSection().getId() != null) {
            Section s = sectionRepository.findById(e.getSection().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Участок не найден"));
            e.setSection(s);
        }
    }
}
