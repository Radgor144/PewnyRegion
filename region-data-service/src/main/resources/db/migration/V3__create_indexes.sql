CREATE INDEX IF NOT EXISTS idx_bdl_variable_year ON bdl_data_records (variable_id, year);
CREATE INDEX IF NOT EXISTS idx_bdl_map ON bdl_data_records (year, variable_id, county_id);
CREATE UNIQUE INDEX uq_single_active_import_job ON import_jobs ((1)) WHERE status IN ('PENDING', 'RUNNING');