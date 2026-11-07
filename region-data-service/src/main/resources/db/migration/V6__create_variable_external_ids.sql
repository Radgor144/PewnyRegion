CREATE TABLE variable_external_ids
(
    id          BIGSERIAL PRIMARY KEY,
    variable_id INTEGER NOT NULL,
    provider    VARCHAR(30) NOT NULL,
    external_id VARCHAR(100) NOT NULL,

    CONSTRAINT fk_variable_external_ids_variable
        FOREIGN KEY (variable_id)
            REFERENCES bdl_variables (id),

    CONSTRAINT uq_variable_external_ids_provider_external
        UNIQUE (provider, external_id)
);

CREATE INDEX idx_variable_external_ids_variable
    ON variable_external_ids (variable_id);

INSERT INTO variable_external_ids (
    variable_id,
    provider,
    external_id
)
SELECT
    bdl_variable_id,
    'BDL',
    bdl_id::VARCHAR
FROM bdl_variable_ids;