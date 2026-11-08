INSERT INTO variables (
    id,
    api_name,
    category,
    direction,
    per_capita,
    unit
)
VALUES
    (1, 'gross_salary', 'FINANCE', 'STIMULANT', FALSE, 'PLN'),
    (2, 'doctors', 'HEALTH', 'STIMULANT', TRUE, 'per_1000'),
--     (3, 'nurses', 'HEALTH', 'STIMULANT', TRUE, 'per_1000'),
--     (4, 'flat_price_avg', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
--     (5, 'flat_price_primary', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
--     (6, 'flat_price_secondary', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
--     (7, 'unemployment', 'LABOR', 'DESTIMULANT', FALSE, '%'),
--     (8, 'population_in_thousands', 'POPULATION', 'STIMULANT', FALSE, 'k'),
--     (9, 'population_density', 'POPULATION', 'STIMULANT', FALSE, 'os/km2'),
--     (10, 'crimes_total', 'SAFETY', 'DESTIMULANT', TRUE, 'per_1000'),
--     (11, 'crimes_per_1000', 'SAFETY', 'DESTIMULANT', FALSE, 'per_1000'),
--     (12, 'crime_detection_rate', 'SAFETY', 'STIMULANT', FALSE, '%'),
--     (13, 'road_accidents_per_100k', 'SAFETY', 'DESTIMULANT', FALSE, 'per_100k'),
--     (14, 'road_fatalities_per_100k', 'SAFETY', 'DESTIMULANT', FALSE, 'per_100k'),
    (15, 'passenger_cars', 'TRANSPORT', 'STIMULANT', TRUE, 'per_1000');


SELECT setval(
               pg_get_serial_sequence('variables', 'id'),
               (SELECT MAX(id) FROM variables),
               TRUE
       );