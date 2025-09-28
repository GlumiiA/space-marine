CREATE TYPE astartes_category AS ENUM (
    'SCOUT',
    'DREADNOUGHT',
    'ASSAULT',
    'SUPPRESSOR',
    'LIBRARIAN'
);

CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          username TEXT NOT NULL UNIQUE,
                          password_hash TEXT NOT NULL,
                          role TEXT NOT NULL DEFAULT 'user'
);

CREATE TABLE chapter (
                         id BIGSERIAL PRIMARY KEY,
                         name TEXT NOT NULL CHECK (trim(name) <> ''),
                         parent_legion TEXT,
                         world TEXT
);

CREATE TABLE coordinates (
                             id BIGSERIAL PRIMARY KEY,
                             x REAL NOT NULL,
                             y REAL NOT NULL
);

CREATE TABLE space_marine (
                              id BIGSERIAL PRIMARY KEY,
                              name TEXT NOT NULL CHECK (trim(name) <> ''),
                              coordinates_id BIGINT NOT NULL REFERENCES coordinates(id) ON DELETE RESTRICT,
                              creation_date TIMESTAMP NOT NULL DEFAULT now(),
                              chapter_id BIGINT NOT NULL REFERENCES chapter(id) ON DELETE RESTRICT,
                              health DOUBLE PRECISION NOT NULL CHECK (health > 0),
                              loyal BOOLEAN NOT NULL,
                              achievements TEXT NOT NULL,
                              category astartes_category NOT NULL
);

CREATE OR REPLACE FUNCTION fn_sum_health() RETURNS DOUBLE PRECISION AS $$
SELECT COALESCE(SUM(health), 0) FROM space_marine;
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION fn_avg_health() RETURNS DOUBLE PRECISION AS $$
SELECT AVG(health) FROM space_marine;
$$ LANGUAGE sql STABLE;
-- сумма health
CREATE OR REPLACE FUNCTION fn_sum_health()
    RETURNS BIGINT AS $$
SELECT COALESCE(SUM(health), 0) FROM space_marine;
$$ LANGUAGE sql;

-- среднее health
CREATE OR REPLACE FUNCTION fn_avg_health()
    RETURNS DOUBLE PRECISION AS $$
SELECT COALESCE(AVG(health), 0) FROM space_marine;
$$ LANGUAGE sql;

-- marine с минимальными координатами
CREATE OR REPLACE FUNCTION fn_min_coordinates()
    RETURNS TABLE(id BIGINT, name VARCHAR, x DOUBLE PRECISION, y DOUBLE PRECISION) AS $$
SELECT sm.id, sm.name, c.x, c.y
FROM space_marine sm
         JOIN coordinates c ON sm.coordinates_id = c.id
ORDER BY c.x ASC, c.y ASC
LIMIT 1;
$$ LANGUAGE sql;
