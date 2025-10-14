package ru.itmo.is.space_marine_backend.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.itmo.is.space_marine_backend.entity.SpaceMarine;

import java.util.List;

@Component
public class MarineFilterStrategyFactory {

    private final List<MarineFilterStrategy> strategies;

    public MarineFilterStrategyFactory(List<MarineFilterStrategy> strategies) {
        this.strategies = strategies;
    }

    public Specification<SpaceMarine> getSpecification(String field, String value) {
        return strategies.stream()
                .filter(s -> s.supports(field))
                .findFirst()
                .map(s -> s.buildSpecification(value))
                .orElse(null);
    }
}