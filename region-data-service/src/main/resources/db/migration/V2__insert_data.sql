INSERT INTO bdl_variables (id, api_name, category, direction, per_capita, unit)
VALUES
    (1, 'gross_salary', 'FINANCE', 'STIMULANT', false, 'PLN'),
    (2, 'doctors', 'HEALTH', 'STIMULANT', true, 'per_1000'),
    (3, 'nurses', 'HEALTH', 'STIMULANT', true, 'per_1000'),
    (4, 'flat_price_avg', 'HOUSING', 'DESTIMULANT', false, 'PLN'),
    (5, 'flat_price_primary', 'HOUSING', 'DESTIMULANT', false, 'PLN'),
    (6, 'flat_price_secondary', 'HOUSING', 'DESTIMULANT', false, 'PLN'),
    (7, 'unemployment', 'LABOR', 'DESTIMULANT', false, '%'),
    (8, 'population_in_thousands', 'POPULATION', 'STIMULANT', false, 'k'),
    (9, 'population_density', 'POPULATION', 'STIMULANT', false, 'os/km2'),
    (10, 'crimes_total', 'SAFETY', 'DESTIMULANT', true, 'per_1000'),
    (11, 'crimes_per_1000', 'SAFETY', 'DESTIMULANT', false, 'per_1000'),
    (12, 'crime_detection_rate', 'SAFETY', 'STIMULANT', false, '%'),
    (13, 'road_accidents_per_100k', 'SAFETY', 'DESTIMULANT', false, 'per_100k'),
    (14, 'road_fatalities_per_100k', 'SAFETY', 'DESTIMULANT', false, 'per_100k'),
    (15, 'passenger_cars', 'TRANSPORT', 'STIMULANT', true, 'per_1000');

INSERT INTO bdl_variable_ids (bdl_variable_id, bdl_id)
VALUES
    (1, 64428),
    (2, 454185),
    (3, 472267),
    (4, 633692),
    (5, 633697),
    (6, 633702),
    (7, 60270),
    (8, 1645341),
    (9, 60559),
    (10, 58559),
    (10, 1749155),
    (11, 398594),
    (11, 1752907),
    (12, 58565),
    (12, 1749170),
    (13, 471916),
    (14, 60551),
    (15, 32561);