package com.example.repairsystem.repository;

import com.example.repairsystem.model.SmsRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SmsRecipientRepository extends JpaRepository<SmsRecipient, Long> {
    List<SmsRecipient> findByActiveTrue();
    List<SmsRecipient> findAllByOrderByNameAsc();
}
