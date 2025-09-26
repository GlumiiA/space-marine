package ru.itmo.is.space_marine_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.is.space_marine_backend.entity.Coordinates;

import java.util.List;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Integer> {
    // поиск по X
    List<Coordinates> findByXGreaterThan(double x);

    // поиск по Y
    List<Coordinates> findByYLessThan(double y);
}
