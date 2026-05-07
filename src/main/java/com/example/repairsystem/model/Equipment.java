package com.example.repairsystem.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String inventoryNumber;
    private String responsiblePerson;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String note;
    private LocalDate commissioningDate; // Дата ввода в эксплуатацию

    // Списание — не удаляем физически, сохраняем историю заявок
    @Column(columnDefinition = "boolean default false")
    private boolean retired = false;
    private LocalDate retiredDate; // Дата списания

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({"equipment","hibernateLazyInitializer"})
    private Section section;

    public Equipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String v) { this.inventoryNumber = v; }

    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String v) { this.responsiblePerson = v; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String v) { this.manufacturer = v; }

    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String v) { this.serialNumber = v; }

    public String getNote() { return note; }
    public void setNote(String v) { this.note = v; }

    public LocalDate getCommissioningDate() { return commissioningDate; }
    public void setCommissioningDate(LocalDate v) { this.commissioningDate = v; }

    public boolean isRetired() { return retired; }
    public void setRetired(boolean retired) { this.retired = retired; }
    public LocalDate getRetiredDate() { return retiredDate; }
    public void setRetiredDate(LocalDate retiredDate) { this.retiredDate = retiredDate; }

    public Section getSection() { return section; }
    public void setSection(Section s) { this.section = s; }

    public String getLocationPath() {
        if (section == null) return null;
        if (section.getBuilding() == null) return section.getName();
        return section.getBuilding().getName() + " / " + section.getName();
    }
}
