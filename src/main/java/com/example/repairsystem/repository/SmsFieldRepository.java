package com.example.repairsystem.repository;

import com.example.repairsystem.model.SmsField;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SmsFieldRepository extends JpaRepository<SmsField, Long> {
    List<SmsField> findByIncludedTrueOrderBySortOrderAsc();
    List<SmsField> findAllByOrderBySortOrderAsc();
}
