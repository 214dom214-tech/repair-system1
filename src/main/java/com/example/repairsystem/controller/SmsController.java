package com.example.repairsystem.controller;

import com.example.repairsystem.model.SmsField;
import com.example.repairsystem.model.SmsRecipient;
import com.example.repairsystem.repository.SmsFieldRepository;
import com.example.repairsystem.repository.SmsRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired SmsRecipientRepository recipientRepo;
    @Autowired SmsFieldRepository     fieldRepo;

    // ═══════════════ ПОЛУЧАТЕЛИ ═══════════════

    @GetMapping("/recipients")
    public List<SmsRecipient> getRecipients() {
        return recipientRepo.findAllByOrderByNameAsc();
    }

    @PostMapping("/recipients")
    @ResponseStatus(HttpStatus.CREATED)
    public SmsRecipient createRecipient(@RequestBody SmsRecipient r) {
        return recipientRepo.save(r);
    }

    @PutMapping("/recipients/{id}")
    public SmsRecipient updateRecipient(@PathVariable Long id, @RequestBody SmsRecipient r) {
        SmsRecipient ex = recipientRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ex.setName(r.getName());
        ex.setEmail(r.getEmail());
        ex.setActive(r.isActive());
        return recipientRepo.save(ex);
    }

    @DeleteMapping("/recipients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipient(@PathVariable Long id) {
        if (!recipientRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        recipientRepo.deleteById(id);
    }

    // ═══════════════ ПОЛЯ SMS ═══════════════

    @GetMapping("/fields")
    public List<SmsField> getFields() {
        return fieldRepo.findAllByOrderBySortOrderAsc();
    }

    @PutMapping("/fields/{id}")
    public SmsField updateField(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SmsField f = fieldRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body.containsKey("included"))
            f.setIncluded(Boolean.TRUE.equals(body.get("included")));
        if (body.containsKey("sortOrder"))
            f.setSortOrder(((Number) body.get("sortOrder")).intValue());
        if (body.containsKey("label"))
            f.setLabel((String) body.get("label"));
        return fieldRepo.save(f);
    }
}
