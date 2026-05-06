package com.example.repairsystem.service;

import com.example.repairsystem.model.*;
import com.example.repairsystem.repository.*;
import com.example.repairsystem.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RequestService {

    @Autowired private RequestRepository        requestRepository;
    @Autowired private EquipmentRepository      equipmentRepository;
    @Autowired private RequestHistoryRepository historyRepository;
    @Autowired(required = false) private SmsService smsService;

    // ── Текущий пользователь ──────────────────────────────────
    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    // ── Запись в историю ──────────────────────────────────────
    private void log(Long requestId, String changeType, String details) {
        historyRepository.save(
            new RequestHistory(requestId, currentUser(), changeType, details));
    }

    // ── Формируем строку изменённых полей ─────────────────────
    private String diffDetails(Request before, Request after) {
        List<String> changes = new ArrayList<>();
        if (!eq(before.getTitle(), after.getTitle()))
            changes.add("Содержание: «" + before.getTitle() + "» → «" + after.getTitle() + "»");
        if (!eq(before.getDepartment(), after.getDepartment()))
            changes.add("Подразделение: «" + before.getDepartment() + "» → «" + after.getDepartment() + "»");
        if (!eq(before.getApplicantName(), after.getApplicantName()))
            changes.add("Заявитель: «" + before.getApplicantName() + "» → «" + after.getApplicantName() + "»");
        if (!eq(before.getNote(), after.getNote()))
            changes.add("Примечание: «" + before.getNote() + "» → «" + after.getNote() + "»");
        if (before.getPriority() != after.getPriority())
            changes.add("Приоритет: " + labelPriority(before.getPriority())
                      + " → " + labelPriority(after.getPriority()));
        if (before.getServiceType() != after.getServiceType())
            changes.add("Служба: " + labelService(before.getServiceType())
                      + " → " + labelService(after.getServiceType()));
        Long eqBefore = before.getEquipment() != null ? before.getEquipment().getId() : null;
        Long eqAfter  = after.getEquipment()  != null ? after.getEquipment().getId()  : null;
        if (!eq(eqBefore, eqAfter)) {
            String nb = before.getEquipment() != null ? before.getEquipment().getName() : "—";
            String na = after.getEquipment()  != null ? after.getEquipment().getName()  : "—";
            changes.add("Оборудование: «" + nb + "» → «" + na + "»");
        }
        return changes.isEmpty() ? "Без изменений" : String.join("; ", changes);
    }

    private boolean eq(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private String labelPriority(RequestPriority p) {
        if (p == null) return "—";
        return p == RequestPriority.EMERGENCY ? "Аварийная" : "Текущая";
    }

    private String labelService(ServiceType s) {
        if (s == null) return "—";
        switch (s) {
            case MECHANICS:    return "Механики";
            case ELECTRICIANS: return "Электрики";
            case ELECTRONICS:  return "Электроники";
            default:           return "Причина неизвестна";
        }
    }

    // ── Публичные методы ──────────────────────────────────────

    public List<Request> getAll() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Request> getByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    public List<Request> getByPriority(RequestPriority priority) {
        return requestRepository.findByPriority(priority);
    }

    public Request getById(Long id) {
        return requestRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Заявка с id=" + id + " не найдена"));
    }

    public Request create(Request request) {
        resolveEquipment(request, request.getEquipment());
        if (request.getStatus() == null)    request.setStatus(RequestStatus.NEW);
        if (request.getPriority() == null)  request.setPriority(RequestPriority.NORMAL);
        if (request.getServiceType() == null) request.setServiceType(ServiceType.UNKNOWN);
        request.setCreatedAt(LocalDateTime.now());
        // Сохраняем автора заявки
        request.setApplicant(currentUser());
        Request saved = requestRepository.save(request);
        log(saved.getId(), "CREATED", "Заявка создана пользователем " + currentUser());
        if (Boolean.TRUE.equals(request.getSendSms()) && smsService != null) {
            new Thread(() -> smsService.sendRequestNotification(saved)).start();
        }
        return saved;
    }

    /**
     * Редактирование заявки.
     * Разрешено только автору заявки (applicant) или ADMIN.
     * Не применяется для смены статуса/службы через специальные методы.
     */
    public Request update(Long id, Request updated) {
        Request existing = getById(id);
        String user = currentUser();

        // Проверка прав: только автор или ADMIN
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !user.equals(existing.getApplicant())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Редактировать заявку может только её автор");
        }

        // Фиксируем что было до изменения
        Request snapshot = copySnapshot(existing);

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setNote(updated.getNote());
        existing.setDepartment(updated.getDepartment());
        existing.setApplicantName(updated.getApplicantName());
        existing.setPriority(updated.getPriority());
        existing.setServiceType(updated.getServiceType());
        resolveEquipment(existing, updated.getEquipment());

        Request saved = requestRepository.save(existing);
        log(id, "EDITED", diffDetails(snapshot, saved));
        return saved;
    }

    /** Принять в работу: NEW → IN_PROGRESS */
    public Request accept(Long id) {
        Request req = getById(id);
        requireStatus(req, RequestStatus.NEW, "Принять в работу");
        req.setStatus(RequestStatus.IN_PROGRESS);
        req.setAcceptedAt(LocalDateTime.now());
        Request saved = requestRepository.save(req);
        log(id, "STATUS_CHANGED", "Статус: Новая → В работе");
        return saved;
    }

    /** Закрыть: IN_PROGRESS → CLOSED */
    public Request close(Long id, String note, RequestPriority priority, Integer downtimeMinutes) {
        Request req = getById(id);
        requireStatus(req, RequestStatus.IN_PROGRESS, "Закрыть");
        req.setStatus(RequestStatus.CLOSED);
        req.setClosedAt(LocalDateTime.now());
        List<String> details = new ArrayList<>();
        details.add("Статус: В работе → Закрыта");
        if (note != null)     { req.setNote(note); details.add("Примечание: «" + note + "»"); }
        if (priority != null) { req.setPriority(priority); details.add("Приоритет: " + labelPriority(priority)); }
        if (downtimeMinutes != null) {
            req.setDowntimeMinutes(downtimeMinutes);
            details.add("Простой: " + formatDowntime(downtimeMinutes));
        }
        Request saved = requestRepository.save(req);
        log(id, "CLOSED", String.join("; ", details));
        return saved;
    }

    /** Вернуть в работу: CLOSED → IN_PROGRESS */
    public Request reopen(Long id) {
        Request req = getById(id);
        requireStatus(req, RequestStatus.CLOSED, "Вернуть в работу");
        req.setStatus(RequestStatus.IN_PROGRESS);
        req.setClosedAt(null);
        Request saved = requestRepository.save(req);
        log(id, "STATUS_CHANGED", "Статус: Закрыта → В работе (возврат)");
        return saved;
    }

    /** Подтвердить: CLOSED → CONFIRMED */
    public Request confirm(Long id) {
        Request req = getById(id);
        requireStatus(req, RequestStatus.CLOSED, "Подтвердить");
        req.setStatus(RequestStatus.CONFIRMED);
        Request saved = requestRepository.save(req);
        log(id, "STATUS_CHANGED", "Статус: Закрыта → Подтверждена");
        return saved;
    }

    /** Сменить службу — без ограничений по автору */
    public Request changeService(Long id, ServiceType newService) {
        Request req = getById(id);
        String old = labelService(req.getServiceType());
        req.setServiceType(newService);
        Request saved = requestRepository.save(req);
        log(id, "SERVICE_CHANGED", "Служба: " + old + " → " + labelService(newService));
        return saved;
    }

    public void delete(Long id) {
        if (!requestRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Заявка с id=" + id + " не найдена");
        requestRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void resolveEquipment(Request target, Equipment ref) {
        if (ref != null && ref.getId() != null) {
            Equipment eq = equipmentRepository.findById(ref.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Оборудование с id=" + ref.getId() + " не найдено"));
            target.setEquipment(eq);
        } else {
            target.setEquipment(null);
        }
    }

    private void requireStatus(Request req, RequestStatus expected, String action) {
        if (req.getStatus() != expected)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                action + " можно только из статуса " + expected
                + " (текущий: " + req.getStatus() + ")");
    }

    private Request copySnapshot(Request r) {
        Request s = new Request();
        s.setTitle(r.getTitle()); s.setDepartment(r.getDepartment());
        s.setApplicantName(r.getApplicantName()); s.setNote(r.getNote());
        s.setPriority(r.getPriority()); s.setServiceType(r.getServiceType());
        s.setEquipment(r.getEquipment());
        return s;
    }

    private String formatDowntime(int minutes) {
        int d = minutes / 1440, h = (minutes % 1440) / 60, m = minutes % 60;
        List<String> p = new ArrayList<>();
        if (d > 0) p.add(d + " дн."); if (h > 0) p.add(h + " ч."); if (m > 0) p.add(m + " мин.");
        return p.isEmpty() ? "0 мин." : String.join(" ", p);
    }
}
