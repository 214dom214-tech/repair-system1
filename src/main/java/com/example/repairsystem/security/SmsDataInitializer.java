package com.example.repairsystem.security;

import com.example.repairsystem.model.SmsField;
import com.example.repairsystem.repository.SmsFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * При первом запуске наполняет справочник полей SMS
 * всеми доступными полями заявки.
 */
@Component
@Order(2)
public class SmsDataInitializer implements CommandLineRunner {

    @Autowired
    private SmsFieldRepository fieldRepo;

    @Override
    public void run(String... args) {
        if (fieldRepo.count() > 0) return; // уже инициализировано

        String[][] fields = {
            {"id",             "Номер заявки",                "1"},
            {"createdAt",      "Дата и время регистрации",    "2"},
            {"department",     "Подразделение",               "3"},
            {"applicantName",  "Заявитель",                   "4"},
            {"priority",       "Приоритет",                   "5"},
            {"title",          "Содержание заявки",           "6"},
            {"serviceType",    "Принадлежность (служба)",     "7"},
            {"equipment",      "Наименование оборудования",   "8"},
            {"inventoryNumber","Инвентарный номер",           "9"},
            {"location",       "Местонахождение",             "10"},
        };

        for (String[] f : fields) {
            SmsField field = new SmsField();
            field.setFieldKey(f[0]);
            field.setLabel(f[1]);
            field.setSortOrder(Integer.parseInt(f[2]));
            field.setIncluded(true);
            fieldRepo.save(field);
        }
        System.out.println("=== SMS поля инициализированы ===");
    }
}
