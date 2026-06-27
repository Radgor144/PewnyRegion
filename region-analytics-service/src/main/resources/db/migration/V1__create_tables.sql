CREATE TABLE IF NOT EXISTS bdl_variables
(
    id        SERIAL PRIMARY KEY,
    api_name  VARCHAR(255) NOT NULL UNIQUE,
    direction VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS bdl_variable_ids
(
    id              SERIAL PRIMARY KEY,
    bdl_variable_id INT NOT NULL,
    bdl_id          INT NOT NULL,
    CONSTRAINT fk_bdl_variable FOREIGN KEY (bdl_variable_id) REFERENCES bdl_variables (id) ON DELETE CASCADE,
    CONSTRAINT unique_bdl_id UNIQUE (bdl_id)
);

CREATE TABLE IF NOT EXISTS counties
(
    id         VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    parent_id  VARCHAR(255),
    level      INTEGER,
    teryt_code VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS bdl_data_records
(
    id             BIGSERIAL PRIMARY KEY,
    county_id      VARCHAR(255)     NOT NULL,
    variable_id    INT              NOT NULL,
    year           INT              NOT NULL,
    value          DOUBLE PRECISION NOT NULL,
    imported_at    TIMESTAMP        NOT NULL,
    score_0_to_100 DOUBLE PRECISION,

    CONSTRAINT fk_bdl_data_county FOREIGN KEY (county_id) REFERENCES counties (id),
    CONSTRAINT fk_bdl_data_variable FOREIGN KEY (variable_id) REFERENCES bdl_variable_ids (bdl_id),
    CONSTRAINT unique_county_variable_year UNIQUE (county_id, variable_id, year)
);

CREATE TABLE IF NOT EXISTS import_jobs
(
    id          VARCHAR(36) PRIMARY KEY,
    job_type    VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    started_at  TIMESTAMP,
    finished_at TIMESTAMP,
    message     TEXT
);
