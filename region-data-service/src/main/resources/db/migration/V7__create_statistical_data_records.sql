CREATE TABLE statistical_data_records
(
    id                              BIGSERIAL PRIMARY KEY,
    territorial_unit_external_id_id BIGINT NOT NULL,
    variable_external_id_id         BIGINT NOT NULL,
    year                            INTEGER NOT NULL,
    value                           DOUBLE PRECISION NOT NULL,
    imported_at                     TIMESTAMP NOT NULL,

    CONSTRAINT fk_statistical_data_unit_external_id
        FOREIGN KEY (territorial_unit_external_id_id)
            REFERENCES territorial_unit_external_ids (id),

    CONSTRAINT fk_statistical_data_variable_external_id
        FOREIGN KEY (variable_external_id_id)
            REFERENCES variable_external_ids (id)
);

CREATE INDEX idx_statistical_data_unit_year
    ON statistical_data_records (territorial_unit_external_id_id, year);

CREATE INDEX idx_statistical_data_variable_year
    ON statistical_data_records (variable_external_id_id, year);

CREATE INDEX idx_statistical_data_imported_at
    ON statistical_data_records (imported_at);