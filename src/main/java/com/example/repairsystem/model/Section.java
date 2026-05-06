package com.example.repairsystem.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Название участка

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"sections","hibernateLazyInitializer"})
    private Building building;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Equipment> equipment;

    public Section() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public Building getBuilding() { return building; }
    public void setBuilding(Building b) { this.building = b; }
    public List<Equipment> getEquipment() { return equipment; }
}
