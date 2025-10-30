package ru.itmo.is.space_marine_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "imports")
@Data
public class ImportOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private LocalDateTime timestamp;
    private String status; // SUCCESS / FAILED
    private Integer addedCount;
}
