package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceMarineRepository extends JpaRepository<SpaceMarine, Long> {


    List<SpaceMarine> findByHealthGreaterThanEqual(Double health);
    List<SpaceMarine> findByCategory(AstartesCategory category);
    List<SpaceMarine> findByLoyal(Boolean loyal);
    List<SpaceMarine> findByChapterNameContainingIgnoreCase(String chapterName);
    List<SpaceMarine> findByChapter(Chapter chapter);


    @Query(value = "SELECT fn_sum_health()", nativeQuery = true)
    Long sumHealth();

    @Query(value = "SELECT fn_avg_health()", nativeQuery = true)
    Double avgHealth();

    @Query(value = "SELECT * FROM fn_min_coordinates()", nativeQuery = true)
    Object findMinCoordinates();

    Page<SpaceMarine> findAll(Specification<SpaceMarine> spec, Pageable pageable);
}