INSERT INTO bdl_variables (api_name)
VALUES ('crimes'),
       ('population_in_thousands'),
       ('population_density'),
       ('unemployment'),
       ('gross_salary');

INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id)
VALUES ((SELECT id FROM bdl_variables WHERE api_name = 'crimes'), 58559),
       ((SELECT id FROM bdl_variables WHERE api_name = 'crimes'), 1749155),
       ((SELECT id FROM bdl_variables WHERE api_name = 'population_in_thousands'), 1645341),
       ((SELECT id FROM bdl_variables WHERE api_name = 'population_density'), 60559),
       ((SELECT id FROM bdl_variables WHERE api_name = 'unemployment'), 60270),
       ((SELECT id FROM bdl_variables WHERE api_name = 'gross_salary'), 64428);