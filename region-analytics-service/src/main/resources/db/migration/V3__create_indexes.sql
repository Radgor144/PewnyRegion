CREATE INDEX IF NOT EXISTS idx_bdl_variable_year ON bdl_data_records (variable_id, year);
CREATE INDEX IF NOT EXISTS idx_bdl_latest_state ON bdl_data_records (county_id, variable_id, year, imported_at DESC);
