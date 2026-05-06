package com.example.repairsystem.controller;

import com.example.repairsystem.model.UserSetting;
import com.example.repairsystem.repository.UserSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
public class UserSettingController {

    @Autowired
    private UserSettingRepository settingRepository;

    /**
     * GET /api/settings
     * Возвращает все настройки текущего пользователя в виде { key: value, ... }
     */
    @GetMapping
    public Map<String, String> getAll(Authentication auth) {
        String username = auth.getName();
        List<UserSetting> settings = settingRepository.findByUsername(username);
        Map<String, String> result = new LinkedHashMap<>();
        settings.forEach(s -> result.put(s.getKey(), s.getValue()));
        return result;
    }

    /**
     * PUT /api/settings
     * Сохраняет/обновляет набор настроек: { key: value, ... }
     * Использует upsert — создаёт новую или обновляет существующую.
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveAll(@RequestBody Map<String, String> settings, Authentication auth) {
        String username = auth.getName();
        settings.forEach((key, value) -> {
            if (key == null || key.isBlank()) return;
            UserSetting setting = settingRepository
                .findByUsernameAndKey(username, key)
                .orElse(new UserSetting(username, key, null));
            setting.setValue(value);
            settingRepository.save(setting);
        });
    }

    /**
     * DELETE /api/settings/{key}
     * Удаляет конкретную настройку.
     */
    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String key, Authentication auth) {
        settingRepository.deleteByUsernameAndKey(auth.getName(), key);
    }
}
