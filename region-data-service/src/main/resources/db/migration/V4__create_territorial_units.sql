CREATE TABLE territorial_units
(
    id              BIGSERIAL PRIMARY KEY,
    teryt_code      VARCHAR(7) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    additional_name VARCHAR(50),
    unit_level      VARCHAR(30) NOT NULL,
    rodz            VARCHAR(1),
    parent_unit_id  BIGINT REFERENCES territorial_units (id),
    source_state_at DATE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_territorial_units_level
        CHECK (unit_level IN ('VOIVODESHIP', 'COUNTY', 'MUNICIPALITY'))
);

CREATE INDEX idx_territorial_units_parent
    ON territorial_units (parent_unit_id);

CREATE INDEX idx_territorial_units_level
    ON territorial_units (unit_level);