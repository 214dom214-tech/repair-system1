package com.example.repairsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_files")
public class EquipmentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    private String originalName;   // оригинальное имя файла
    private String storedName;     // имя на диске (UUID)
    private String contentType;    // MIME-тип
    private Long fileSize;         // размер в байтах

    public EquipmentFile() {}

    public Long getId() { return id; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
