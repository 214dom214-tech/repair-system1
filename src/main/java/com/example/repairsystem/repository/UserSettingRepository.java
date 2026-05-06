package com.example.repairsystem.repository;

import com.example.repairsystem.model.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    List<UserSetting> findByUsername(String username);

    Optional<UserSetting> findByUsernameAndKey(String username, String key);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSetting s WHERE s.username = :username AND s.key = :key")
    void deleteByUsernameAndKey(String username, String key);
}
