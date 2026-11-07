CREATE TABLE territorial_unit_external_ids
(
    id                  BIGSERIAL PRIMARY KEY,
    territorial_unit_id BIGINT NOT NULL,
    provider            VARCHAR(30) NOT NULL,
    external_id         VARCHAR(100) NOT NULL,
    provider_level      VARCHAR(30),

    CONSTRAINT fk_territorial_unit_external_ids_unit
        FOREIGN KEY (territorial_unit_id)
            REFERENCES territorial_units (id),

    CONSTRAINT uq_territorial_unit_external_ids_provider_external
        UNIQUE (provider, external_id)
);

CREATE INDEX idx_territorial_unit_external_ids_unit
    ON territorial_unit_external_ids (territorial_unit_id);

CREATE INDEX idx_territorial_unit_external_ids_provider
    ON territorial_unit_external_ids (provider);