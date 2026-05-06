package com.example.repairsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sms_fields")
public class SmsField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fieldKey;    // Технический ключ поля заявки

    @Column(nullable = false)
    private String label;       // Читаемое название поля

    private boolean included = true;  // Включать в SMS

    private int sortOrder = 0;  // Порядок вывода

    public SmsField() {}

    public Long getId() { return id; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public boolean isIncluded() { return included; }
    public void setIncluded(boolean included) { this.included = included; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
