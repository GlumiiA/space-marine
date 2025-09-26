package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceMarineRepository extends JpaRepository<SpaceMarine, Long> {

    // Фильтрация
    Page<SpaceMarine> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<SpaceMarine> findByAchievementsContainingIgnoreCase(String achievements, Pageable pageable);
    List<SpaceMarine> findByHealthGreaterThan(Double health);
    List<SpaceMarine> findByHealthGreaterThanEqual(Double health);
    List<SpaceMarine> findByCategory(AstartesCategory category);
    List<SpaceMarine> findByLoyal(Boolean loyal);
    List<SpaceMarine> findByChapterNameContainingIgnoreCase(String chapterName);

    // TODO: Специальные операции (заглушки - нужно будет реализовать функции в БД)
    @Query(value = "SELECT SUM(health) FROM space_marine", nativeQuery = true)
    Double calculateTotalHealth();

    @Query(value = "SELECT AVG(health) FROM space_marine", nativeQuery = true)
    Double calculateAverageHealth();

    @Query(value = "SELECT * FROM space_marine ORDER BY (coordinates_x * coordinates_x + coordinates_y * coordinates_y) ASC LIMIT 1", nativeQuery = true)
    Optional<SpaceMarine> findMarineWithMinCoordinates();
}