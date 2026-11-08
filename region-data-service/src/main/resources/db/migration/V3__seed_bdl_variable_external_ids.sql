INSERT INTO variable_external_ids (
    variable_id,
    provider,
    external_id
)
SELECT
    v.id,
    'BDL',
    mapping.external_id
FROM (
         VALUES
             ('gross_salary', '64428'),
             ('doctors', '454185'),
             ('nurses', '472267'),
             ('flat_price_avg', '633692'),
             ('flat_price_primary', '633697'),
             ('flat_price_secondary', '633702'),
             ('unemployment', '60270'),
             ('population_in_thousands', '1645341'),
             ('population_density', '60559'),
             ('crimes_total', '58559'),
             ('crimes_total', '1749155'),
             ('crimes_per_1000', '398594'),
             ('crimes_per_1000', '1752907'),
             ('crime_detection_rate', '58565'),
             ('crime_detection_rate', '1749170'),
             ('road_accidents_per_100k', '471916'),
             ('road_fatalities_per_100k', '60551'),
             ('passenger_cars', '32561')
     ) AS mapping(api_name, external_id)
         JOIN variables v
              ON v.api_name = mapping.api_name;