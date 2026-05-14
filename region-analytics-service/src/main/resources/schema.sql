CREATE TABLE IF NOT EXISTS counties (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    parent_id VARCHAR(255),
    level INTEGER,
    teryt VARCHAR(10)
);