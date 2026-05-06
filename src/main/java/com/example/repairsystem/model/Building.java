package com.example.repairsystem.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Название корпуса

    private String description;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Section> sections;

    public Building() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public List<Section> getSections() { return sections; }
}
