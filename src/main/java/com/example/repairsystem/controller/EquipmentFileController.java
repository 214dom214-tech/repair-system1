package com.example.repairsystem.controller;

import com.example.repairsystem.model.Equipment;
import com.example.repairsystem.model.EquipmentFile;
import com.example.repairsystem.model.EquipmentFileDto;
import com.example.repairsystem.repository.EquipmentFileRepository;
import com.example.repairsystem.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment/{equipmentId}/files")
public class EquipmentFileController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Autowired private EquipmentFileRepository fileRepository;
    @Autowired private EquipmentRepository equipmentRepository;

    /** GET — список файлов оборудования */
    @GetMapping
    public List<EquipmentFileDto> listFiles(@PathVariable Long equipmentId) {
        return fileRepository.findByEquipmentId(equipmentId)
                .stream().map(EquipmentFileDto::new).collect(Collectors.toList());
    }

    /** POST — загрузить файл */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentFileDto upload(@PathVariable Long equipmentId,
                                   @RequestParam("file") MultipartFile file) throws IOException {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Оборудование с id=" + equipmentId + " не найдено"));

        // Создаём папку если нет
        Path dir = Paths.get(uploadDir, "equipment", String.valueOf(equipmentId));
        Files.createDirectories(dir);

        // Уникальное имя на диске
        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains("."))
            ext = orig.substring(orig.lastIndexOf('.'));
        String storedName = UUID.randomUUID() + ext;

        Files.copy(file.getInputStream(), dir.resolve(storedName),
                StandardCopyOption.REPLACE_EXISTING);

        EquipmentFile ef = new EquipmentFile();
        ef.setEquipment(equipment);
        ef.setOriginalName(orig);
        ef.setStoredName(storedName);
        ef.setContentType(file.getContentType());
        ef.setFileSize(file.getSize());

        return new EquipmentFileDto(fileRepository.save(ef));
    }

    /** GET /{fileId}/download — скачать файл */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long equipmentId,
                                              @PathVariable Long fileId) throws MalformedURLException {
        EquipmentFile ef = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден"));

        Path path = Paths.get(uploadDir, "equipment",
                String.valueOf(equipmentId), ef.getStoredName());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден на диске");

        String contentType = ef.getContentType() != null
                ? ef.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" +
                        java.net.URLEncoder.encode(ef.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8)
                                .replace("+", "%20"))
                .body(resource);
    }

    /** DELETE /{fileId} — удалить файл */
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long equipmentId,
                       @PathVariable Long fileId) throws IOException {
        EquipmentFile ef = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден"));

        Path path = Paths.get(uploadDir, "equipment",
                String.valueOf(equipmentId), ef.getStoredName());
        Files.deleteIfExists(path);
        fileRepository.deleteById(fileId);
    }
}
