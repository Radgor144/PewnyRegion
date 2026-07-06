CREATE INDEX IF NOT EXISTS idx_bdl_variable_year ON bdl_data_records (variable_id, year);
CREATE INDEX IF NOT EXISTS idx_bdl_map ON bdl_data_records (year, variable_id, county_id);
