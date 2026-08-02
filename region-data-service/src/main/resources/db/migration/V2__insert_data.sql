INSERT INTO bdl_variables (id, api_name, direction, per_capita)
VALUES (1, 'crimes', 'DESTIMULANT', true),
       (2, 'population_in_thousands', 'STIMULANT', false),
       (3, 'population_density', 'STIMULANT', false),
       (4, 'unemployment', 'DESTIMULANT', false),
       (5, 'gross_salary', 'STIMULANT', false);

INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id)
VALUES (1, 58559),
       (1, 1749155),
       (2, 1645341),
       (3, 60559),
       (4, 60270),
       (5, 64428);
