INSERT INTO variables (
    api_name,
    category,
    direction,
    per_capita,
    unit
)
VALUES
    ('gross_salary', 'FINANCE', 'STIMULANT', FALSE, 'PLN'),
    ('doctors', 'HEALTH', 'STIMULANT', TRUE, 'per_1000'),
    ('nurses', 'HEALTH', 'STIMULANT', TRUE, 'per_1000'),
    ('flat_price_avg', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
    ('flat_price_primary', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
    ('flat_price_secondary', 'HOUSING', 'DESTIMULANT', FALSE, 'PLN'),
    ('unemployment', 'LABOR', 'DESTIMULANT', FALSE, '%'),
    ('population_in_thousands', 'POPULATION', 'STIMULANT', FALSE, 'k'),
    ('population_density', 'POPULATION', 'STIMULANT', FALSE, 'os/km2'),
    ('crimes_total', 'SAFETY', 'DESTIMULANT', TRUE, 'per_1000'),
    ('crimes_per_1000', 'SAFETY', 'DESTIMULANT', FALSE, 'per_1000'),
    ('crime_detection_rate', 'SAFETY', 'STIMULANT', FALSE, '%'),
    ('road_accidents_per_100k', 'SAFETY', 'DESTIMULANT', FALSE, 'per_100k'),
    ('road_fatalities_per_100k', 'SAFETY', 'DESTIMULANT', FALSE, 'per_100k'),
    ('passenger_cars', 'TRANSPORT', 'STIMULANT', TRUE, 'per_1000');
