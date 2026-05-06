package com.example.repairsystem.controller;

import com.example.repairsystem.model.Equipment;
import com.example.repairsystem.repository.EquipmentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/equipment/import")
public class EquipmentExcelController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    /**
     * POST /api/equipment/import
     * Формат: Инв.номер | Наименование | Дата ввода (дд.мм.гггг) | МОЛ
     *
     * Логика upsert по инвентарному номеру:
     *  - Если оборудование с таким инв.номером уже есть → обновляем название/МОЛ/дату
     *  - Если нет → создаём новое
     *  - Оборудование которого нет в файле — НЕ трогаем (не списываем автоматически)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) return error("Файл не выбран");
        String fn = file.getOriginalFilename();
        if (fn == null || !fn.toLowerCase().endsWith(".xlsx"))
            return error("Поддерживаются только файлы .xlsx");

        List<String> created  = new ArrayList<>();
        List<String> updated  = new ArrayList<>();
        List<String> errors   = new ArrayList<>();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (InputStream is = file.getInputStream();
             Workbook wb    = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNum  = 0;

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // заголовок
                if (isRowEmpty(row)) continue;

                String invNumber = cellStr(row, 0);
                String name      = cellStr(row, 1);
                String dateStr   = cellStr(row, 2);
                String mol       = cellStr(row, 3);

                if (name.isBlank()) {
                    errors.add("Строка " + rowNum + ": пустое наименование — пропущена");
                    continue;
                }

                LocalDate commDate = null;
                if (!dateStr.isBlank()) {
                    try { commDate = parseDate(dateStr, dtf); }
                    catch (Exception e) {
                        errors.add("Строка " + rowNum + " («" + name + "»): неверный формат даты «"
                                + dateStr + "» — ожидается дд.мм.гггг");
                    }
                }

                if (!invNumber.isBlank()) {
                    // Upsert по инвентарному номеру
                    Optional<Equipment> existing =
                        equipmentRepository.findByInventoryNumber(invNumber);

                    if (existing.isPresent()) {
                        // Обновляем существующее — инв.номер остаётся, id не меняется
                        Equipment eq = existing.get();
                        eq.setName(name);
                        if (!mol.isBlank())     eq.setResponsiblePerson(mol);
                        if (commDate != null)   eq.setCommissioningDate(commDate);
                        eq.setRetired(false);   // при повторном появлении — снимаем списание
                        eq.setRetiredDate(null);
                        equipmentRepository.save(eq);
                        updated.add(name + " [" + invNumber + "]");
                    } else {
                        // Создаём новое
                        Equipment eq = buildEquipment(name, invNumber, mol, commDate);
                        equipmentRepository.save(eq);
                        created.add(name + " [" + invNumber + "]");
                    }
                } else {
                    // Без инв.номера — просто создаём (дубли возможны, предупреждаем)
                    Equipment eq = buildEquipment(name, null, mol, commDate);
                    equipmentRepository.save(eq);
                    created.add(name + " (без инв.номера)");
                }
            }

        } catch (Exception e) {
            return error("Ошибка чтения файла: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created",  created.size());
        result.put("updated",  updated.size());
        result.put("errors",   errors.size());
        result.put("createdList", created);
        result.put("updatedList", updated);
        result.put("errorList",   errors);
        return ResponseEntity.ok(result);
    }

    private Equipment buildEquipment(String name, String inv, String mol, LocalDate date) {
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setInventoryNumber(inv);
        eq.setResponsiblePerson(mol.isBlank() ? null : mol);
        eq.setCommissioningDate(date);
        return eq;
    }

    private String cellStr(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toLocalDate()
                               .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                double d = cell.getNumericCellValue();
                return d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            case FORMULA:
                try { return cell.getStringCellValue().trim(); }
                catch (Exception ex) { return String.valueOf((long) cell.getNumericCellValue()); }
            default: return "";
        }
    }

    private LocalDate parseDate(String s, DateTimeFormatter primary) {
        try { return LocalDate.parse(s, primary); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")); } catch (DateTimeParseException ignored) {}
        throw new IllegalArgumentException("Не удалось распознать дату: " + s);
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row)
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty()) return false;
        return true;
    }

    private ResponseEntity<Map<String, Object>> error(String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", msg);
        return ResponseEntity.badRequest().body(body);
    }
}
