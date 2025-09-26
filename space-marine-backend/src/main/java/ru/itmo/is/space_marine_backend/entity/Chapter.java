package ru.itmo.is.space_marine_backend.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "chapter")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String parentLegion;
    private String world;
    private Integer marinesCount;
}

