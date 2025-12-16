package ru.itmo.is.space_marine_backend.repository;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.AstartesCategory;
import ru.itmo.is.space_marine_backend.entity.Chapter;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface SpaceMarineRepository extends JpaRepository<SpaceMarine, Long> {

    @QueryHints(@QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    List<SpaceMarine> findByHealthGreaterThanEqual(Double health);

    @QueryHints(@QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    List<SpaceMarine> findByCategory(AstartesCategory category);

    @QueryHints(@QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    List<SpaceMarine> findByIsLoyal(Boolean isLoyal);

    @QueryHints(@QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    List<SpaceMarine> findByChapterNameContainingIgnoreCase(String chapterName);

    @QueryHints(@QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    List<SpaceMarine> findByChapter(Chapter chapter);

    @Query(value = "SELECT fn_sum_health()", nativeQuery = true)
    Long sumHealth();

    @Query(value = "SELECT fn_avg_health()", nativeQuery = true)
    Double avgHealth();

    @Query(value = "SELECT * FROM fn_min_coordinates()", nativeQuery = true)
    Object findMinCoordinates();

    Page<SpaceMarine> findAll(Specification<SpaceMarine> spec, Pageable pageable);
}