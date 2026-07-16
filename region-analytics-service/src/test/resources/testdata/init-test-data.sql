-- Clean up transactional tables to isolate tests
DELETE FROM county_variable_scores;
DELETE FROM counties;

-- Insert target counties
INSERT INTO counties (id, name, parent_id, level, teryt_code)
VALUES
    ('011212019000', 'Powiat wielicki',   '011212000000', 5, '1219'),
    ('011212006000', 'Powiat krakowski',  '011212000000', 5, '1206'),
    ('011212008000', 'Powiat miechowski', '011212000000', 5, '1208');

-- ==========================================
-- VARIABLE 1: CRIMES (bdl_variable_id = 1)
-- Data timeline: 2015 - 2018
-- ==========================================

-- Year 2015
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 1, 2015, 18500, 38.12, 22.10, '2026-07-16 10:00:00'),
    ('011212006000', 1, 2015,   950, 16.20, 51.50, '2026-07-16 10:00:00'),
    ('011212008000', 1, 2015,   880, 18.50, 49.10, '2026-07-16 10:00:00');

-- Year 2016
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 1, 2016, 17200, 35.40, 25.80, '2026-07-16 10:00:00'),
    ('011212006000', 1, 2016,   910, 15.50, 54.20, '2026-07-16 10:00:00'),
    ('011212008000', 1, 2016,   850, 17.90, 51.30, '2026-07-16 10:00:00');

-- Year 2017
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 1, 2017, 16100, 33.10, 28.90, '2026-07-16 10:00:00'),
    ('011212006000', 1, 2017,   890, 15.15, 55.40, '2026-07-16 10:00:00'),
    ('011212008000', 1, 2017,   820, 17.25, 54.00, '2026-07-16 10:00:00');

-- Year 2018
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 1, 2018, 15553, 31.91, 31.50, '2026-07-16 10:00:00'),
    ('011212006000', 1, 2018,   854, 14.94, 57.63, '2026-07-16 10:00:00'),
    ('011212008000', 1, 2018,   817, 17.50, 53.25, '2026-07-16 10:00:00');


-- ==========================================
-- VARIABLE 5: GROSS SALARY (bdl_variable_id = 5)
-- Data timeline: 2015 - 2018 (visible salary growth)
-- ==========================================

-- Year 2015
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 5, 2015, 4100.50, 4100.50, 68.50, '2026-07-16 10:05:00'),
    ('011212006000', 5, 2015, 3400.20, 3400.20, 49.10, '2026-07-16 10:05:00'),
    ('011212008000', 5, 2015, 3100.80, 3100.80, 38.20, '2026-07-16 10:05:00');

-- Year 2016
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 5, 2016, 4350.00, 4350.00, 70.20, '2026-07-16 10:05:00'),
    ('011212006000', 5, 2016, 3620.40, 3620.40, 51.80, '2026-07-16 10:05:00'),
    ('011212008000', 5, 2016, 3250.10, 3250.10, 39.90, '2026-07-16 10:05:00');

-- Year 2017
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 5, 2017, 4600.10, 4600.10, 72.80, '2026-07-16 10:05:00'),
    ('011212006000', 5, 2017, 3800.90, 3800.90, 53.40, '2026-07-16 10:05:00'),
    ('011212008000', 5, 2017, 3410.50, 3410.50, 41.50, '2026-07-16 10:05:00');

-- Year 2018
INSERT INTO county_variable_scores (county_id, bdl_variable_id, year, raw_value, adjusted_value, normalized_score, calculated_at)
VALUES
    ('011212019000', 5, 2018, 4900.80, 4900.80, 75.40, '2026-07-16 10:05:00'),
    ('011212006000', 5, 2018, 4110.30, 4110.30, 56.10, '2026-07-16 10:05:00'),
    ('011212008000', 5, 2018, 3600.20, 3600.20, 43.80, '2026-07-16 10:05:00');