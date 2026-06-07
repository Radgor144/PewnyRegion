CREATE TABLE counties(
    id         VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    parent_id  VARCHAR(255),
    level      INTEGER,
    teryt_code VARCHAR(10)
);