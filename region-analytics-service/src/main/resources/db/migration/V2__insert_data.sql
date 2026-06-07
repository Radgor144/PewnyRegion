-- 1. Wstawiamy kategorie i pobieramy ich wygenerowane ID
INSERT INTO bdl_variables (api_name) VALUES ('crimes');
INSERT INTO bdl_variables (api_name) VALUES ('population_in_thousands');
INSERT INTO bdl_variables (api_name) VALUES ('population_density');
INSERT INTO bdl_variables (api_name) VALUES ('unemployment');
INSERT INTO bdl_variables (api_name) VALUES ('gross_salary');

-- 2. Wstawiamy poszczególne ID powiązane z kategoriami
-- Dla crimes (zakładając id = 1):
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (1, 58559);
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (1, 1749155);

-- Dla reszty analogicznie (id = 2, 3, 4, 5):
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (2, 1645341);
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (3, 60559);
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (4, 60270);
INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id) VALUES (5, 64428);