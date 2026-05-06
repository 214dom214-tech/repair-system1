package com.example.repairsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "setting_key"}))
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    public UserSetting() {}

    public UserSetting(String username, String key, String value) {
        this.username = username;
        this.key      = key;
        this.value    = value;
    }

    public Long getId()       { return id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getKey()    { return key; }
    public void setKey(String k) { this.key = k; }
    public String getValue()  { return value; }
    public void setValue(String v) { this.value = v; }
}
