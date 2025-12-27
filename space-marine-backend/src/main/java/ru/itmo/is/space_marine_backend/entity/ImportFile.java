package ru.itmo.is.space_marine_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "import_file")
public class ImportFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportOperation importOperation;

    private String txId;
    private String ownerUsername;
    private String fileName;
    private String bucket;
    private String objectKey;
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    private StorageStatus storageStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
