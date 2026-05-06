package com.example.repairsystem.service;

import com.example.repairsystem.model.Request;
import com.example.repairsystem.model.SmsField;
import com.example.repairsystem.model.SmsRecipient;
import com.example.repairsystem.repository.SmsFieldRepository;
import com.example.repairsystem.repository.SmsRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SmsService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private SmsRecipientRepository recipientRepository;

    @Autowired
    private SmsFieldRepository fieldRepository;

    @Value("${spring.mail.username:noreply@repair-system.local}")
    private String fromEmail;

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Отправляет SMS (через email) всем активным получателям.
     * Состав сообщения определяется справочником SmsField.
     */
    public void sendRequestNotification(Request req) {
        if (mailSender == null) {
            System.err.println("[SMS] JavaMailSender не настроен — отправка пропущена");
            return;
        }

        List<SmsRecipient> recipients = recipientRepository.findByActiveTrue();
        if (recipients.isEmpty()) return;

        List<SmsField> fields = fieldRepository.findByIncludedTrueOrderBySortOrderAsc();
        String text = buildText(req, fields);

        for (SmsRecipient recipient : recipients) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(recipient.getEmail());
                msg.setSubject("Новая заявка #" + req.getId());
                msg.setText(text);
                mailSender.send(msg);
                System.out.println("[SMS] Отправлено на " + recipient.getEmail());
            } catch (Exception e) {
                System.err.println("[SMS] Ошибка отправки на "
                    + recipient.getEmail() + ": " + e.getMessage());
            }
        }
    }

    /** Формирует текст SMS по выбранным полям */
    private String buildText(Request req, List<SmsField> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("Заявка #").append(req.getId()).append("\n");

        for (SmsField f : fields) {
            String value = resolveField(req, f.getFieldKey());
            if (value != null && !value.isBlank()) {
                sb.append(f.getLabel()).append(": ").append(value).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /** Возвращает значение поля заявки по ключу */
    private String resolveField(Request req, String key) {
        switch (key) {
            case "id":            return req.getId() != null ? "#" + req.getId() : null;
            case "title":         return req.getTitle();
            case "department":    return req.getDepartment();
            case "applicantName": return req.getApplicantName();
            case "priority":      return req.getPriority() != null
                                    ? (req.getPriority().name().equals("EMERGENCY")
                                        ? "Аварийная" : "Текущая") : null;
            case "serviceType":   return resolveService(req);
            case "equipment":     return req.getEquipment() != null
                                    ? req.getEquipment().getName() : null;
            case "inventoryNumber": return req.getEquipment() != null
                                    ? req.getEquipment().getInventoryNumber() : null;
            case "location":      return resolveLocation(req);
            case "createdAt":     return req.getCreatedAt() != null
                                    ? req.getCreatedAt().format(DTF) : null;
            default: return null;
        }
    }

    private String resolveService(Request req) {
        if (req.getServiceType() == null) return null;
        switch (req.getServiceType()) {
            case MECHANICS:    return "Механики";
            case ELECTRICIANS: return "Электрики";
            case ELECTRONICS:  return "Электроники";
            default:           return "Причина неизвестна";
        }
    }

    private String resolveLocation(Request req) {
        if (req.getEquipment() == null || req.getEquipment().getSection() == null) return null;
        var s = req.getEquipment().getSection();
        if (s.getBuilding() != null)
            return s.getBuilding().getName() + ", " + s.getName();
        return s.getName();
    }
}
