package ru.itmo.is.space_marine_backend.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "space_marine")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SpaceMarine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Column(nullable = false)
    private double health;

    @Column(name = "is_loyal", nullable = false)
    private boolean isLoyal;

    @Column(nullable = false)
    private String achievements;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AstartesCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
