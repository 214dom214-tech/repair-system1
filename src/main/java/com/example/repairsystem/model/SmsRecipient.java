package com.example.repairsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sms_recipients")
public class SmsRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // Имя/описание получателя

    @Column(nullable = false)
    private String email;       // Email-to-SMS шлюз (например: 79001234567@sms.ru)

    private boolean active = true; // Активен ли получатель

    public SmsRecipient() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
