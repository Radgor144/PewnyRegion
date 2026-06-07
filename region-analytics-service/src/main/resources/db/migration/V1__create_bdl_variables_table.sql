CREATE TABLE IF NOT EXISTS bdl_variables
(
    id SERIAL PRIMARY KEY,
    api_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS bdl_variable_ids
(
    id SERIAL PRIMARY KEY,
    bdl_variable_id INT NOT NULL,
    bdl_id INT NOT NULL,
    CONSTRAINT fk_bdl_variable FOREIGN KEY (bdl_variable_id) REFERENCES bdl_variables (id) ON DELETE CASCADE
);