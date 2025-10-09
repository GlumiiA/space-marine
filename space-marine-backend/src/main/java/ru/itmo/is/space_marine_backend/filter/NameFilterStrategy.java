package ru.itmo.is.space_marine_backend.filter;

import org.springframework.data.jpa.domain.Specification;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

public class NameFilterStrategy implements MarineFilterStrategy {

    @Override
    public boolean supports(String field) {
        return "name".equalsIgnoreCase(field);
    }

    @Override
    public Specification<SpaceMarine> buildSpecification(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), String.format("%%%s%%", value.toLowerCase()));
    }
}