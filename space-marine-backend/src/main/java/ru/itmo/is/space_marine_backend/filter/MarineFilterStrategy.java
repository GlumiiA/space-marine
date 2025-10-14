package ru.itmo.is.space_marine_backend.filter;

import org.springframework.data.jpa.domain.Specification;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

public interface MarineFilterStrategy {
    boolean supports(String field);
    Specification<SpaceMarine> buildSpecification(String value);
}