package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.Chapter;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> findByName(String name);
    Boolean existsByName(String name);
}
