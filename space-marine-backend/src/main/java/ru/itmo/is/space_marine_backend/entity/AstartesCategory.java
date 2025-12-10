package ru.itmo.is.space_marine_backend.entity;

public enum AstartesCategory {
    SCOUT(0.5),
    ASSAULT(1.0),
    SUPPRESSOR(1.2),
    LIBRARIAN(1.0),
    DREADNOUGHT(2.0);

    private final double radius;

    AstartesCategory(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }
}
