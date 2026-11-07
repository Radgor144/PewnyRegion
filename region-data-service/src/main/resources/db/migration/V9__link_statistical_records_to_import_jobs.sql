
ALTER TABLE import_jobs
    ADD COLUMN provider VARCHAR(30);

ALTER TABLE statistical_data_records
    ADD COLUMN import_job_id VARCHAR(36);

ALTER TABLE statistical_data_records
    ADD CONSTRAINT fk_statistical_data_records_import_job
        FOREIGN KEY (import_job_id)
            REFERENCES import_jobs (id);

CREATE INDEX idx_statistical_data_records_import_job
    ON statistical_data_records (import_job_id);
