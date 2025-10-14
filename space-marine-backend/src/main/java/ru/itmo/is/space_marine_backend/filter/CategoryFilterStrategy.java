package ru.itmo.is.space_marine_backend.filter;


import org.springframework.data.jpa.domain.Specification;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

public class CategoryFilterStrategy implements MarineFilterStrategy {

    @Override
    public boolean supports(String field) {
        return "category".equalsIgnoreCase(field);
    }

    @Override
    public Specification<SpaceMarine> buildSpecification(String value) {
        return (root, query, cb) ->
                cb.equal(root.get("category"), value.toUpperCase());
    }
}