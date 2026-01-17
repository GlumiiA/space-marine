package ru.itmo.is.space_marine_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "import_file")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
