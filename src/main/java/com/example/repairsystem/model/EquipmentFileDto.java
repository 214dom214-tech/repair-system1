package com.example.repairsystem.model;

// DTO для передачи файла на фронтенд без lazy-полей
public class EquipmentFileDto {
    private Long id;
    private String originalName;
    private String contentType;
    private Long fileSize;

    public EquipmentFileDto(EquipmentFile f) {
        this.id           = f.getId();
        this.originalName = f.getOriginalName();
        this.contentType  = f.getContentType();
        this.fileSize     = f.getFileSize();
    }

    public Long getId()           { return id; }
    public String getOriginalName() { return originalName; }
    public String getContentType()  { return contentType; }
    public Long getFileSize()       { return fileSize; }
}
