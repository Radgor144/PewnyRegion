DELETE FROM territorial_unit_variable_scores
WHERE calculation_job_id = 'map-county-scores-test-job';

DELETE FROM territorial_units
WHERE teryt_code IN ('1206', '1208', '1219');

DELETE FROM import_jobs
WHERE id = 'map-county-scores-test-job';

INSERT INTO territorial_units (teryt_code, name, unit_level)
VALUES
    ('1219', 'Powiat wielicki', 'COUNTY'),
    ('1206', 'Powiat krakowski', 'COUNTY'),
    ('1208', 'Powiat miechowski', 'COUNTY');

INSERT INTO import_jobs (id, job_type, provider, status, started_at, finished_at)
VALUES (
    'map-county-scores-test-job',
    'NORMALIZATION',
    'BDL',
    'COMPLETED',
    '2026-07-16 10:00:00',
    '2026-07-16 10:10:00'
);

INSERT INTO territorial_unit_variable_scores (
    territorial_unit_id,
    variable_id,
    year,
    raw_value,
    adjusted_value,
    normalized_score,
    calculation_job_id,
    calculated_at
)
SELECT
    unit.id,
    variable.id,
    record.year,
    record.raw_value,
    record.adjusted_value,
    record.normalized_score,
    'map-county-scores-test-job',
    record.calculated_at
FROM (
    VALUES
        ('1219', 'crimes_total', 2015, 18500, 38.12, 0.2210, TIMESTAMP '2026-07-16 10:00:00'),
        ('1206', 'crimes_total', 2015,   950, 16.20, 0.5150, TIMESTAMP '2026-07-16 10:00:00'),
        ('1208', 'crimes_total', 2015,   880, 18.50, 0.4910, TIMESTAMP '2026-07-16 10:00:00'),
        ('1219', 'crimes_total', 2016, 17200, 35.40, 0.2580, TIMESTAMP '2026-07-16 10:00:00'),
        ('1206', 'crimes_total', 2016,   910, 15.50, 0.5420, TIMESTAMP '2026-07-16 10:00:00'),
        ('1208', 'crimes_total', 2016,   850, 17.90, 0.5130, TIMESTAMP '2026-07-16 10:00:00'),
        ('1219', 'crimes_total', 2017, 16100, 33.10, 0.2890, TIMESTAMP '2026-07-16 10:00:00'),
        ('1206', 'crimes_total', 2017,   890, 15.15, 0.5540, TIMESTAMP '2026-07-16 10:00:00'),
        ('1208', 'crimes_total', 2017,   820, 17.25, 0.5400, TIMESTAMP '2026-07-16 10:00:00'),
        ('1219', 'crimes_total', 2018, 15553, 31.91, 0.3150, TIMESTAMP '2026-07-16 10:00:00'),
        ('1206', 'crimes_total', 2018,   854, 14.94, 0.5763, TIMESTAMP '2026-07-16 10:00:00'),
        ('1208', 'crimes_total', 2018,   817, 17.50, 0.5325, TIMESTAMP '2026-07-16 10:00:00'),
        ('1219', 'gross_salary', 2015, 4100.50, 4100.50, 0.6850, TIMESTAMP '2026-07-16 10:05:00'),
        ('1206', 'gross_salary', 2015, 3400.20, 3400.20, 0.4910, TIMESTAMP '2026-07-16 10:05:00'),
        ('1208', 'gross_salary', 2015, 3100.80, 3100.80, 0.3820, TIMESTAMP '2026-07-16 10:05:00'),
        ('1219', 'gross_salary', 2016, 4350.00, 4350.00, 0.7020, TIMESTAMP '2026-07-16 10:05:00'),
        ('1206', 'gross_salary', 2016, 3620.40, 3620.40, 0.5180, TIMESTAMP '2026-07-16 10:05:00'),
        ('1208', 'gross_salary', 2016, 3250.10, 3250.10, 0.3990, TIMESTAMP '2026-07-16 10:05:00'),
        ('1219', 'gross_salary', 2017, 4600.10, 4600.10, 0.7280, TIMESTAMP '2026-07-16 10:05:00'),
        ('1206', 'gross_salary', 2017, 3800.90, 3800.90, 0.5340, TIMESTAMP '2026-07-16 10:05:00'),
        ('1208', 'gross_salary', 2017, 3410.50, 3410.50, 0.4150, TIMESTAMP '2026-07-16 10:05:00'),
        ('1219', 'gross_salary', 2018, 4900.80, 4900.80, 0.7540, TIMESTAMP '2026-07-16 10:05:00'),
        ('1206', 'gross_salary', 2018, 4110.30, 4110.30, 0.5610, TIMESTAMP '2026-07-16 10:05:00'),
        ('1208', 'gross_salary', 2018, 3600.20, 3600.20, 0.4380, TIMESTAMP '2026-07-16 10:05:00')
) AS record(teryt_code, api_name, year, raw_value, adjusted_value, normalized_score, calculated_at)
JOIN territorial_units unit
    ON unit.teryt_code = record.teryt_code
JOIN variables variable
    ON variable.api_name = record.api_name;
