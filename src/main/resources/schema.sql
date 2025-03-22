-- Drop table if exists
-- DROP TABLE IF EXISTS file_data;

-- Create file_data table with explicit bytea type
CREATE TABLE IF NOT EXISTS file_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL UNIQUE,
    data BYTEA NOT NULL,
    created_at TIMESTAMP
);

-- Create index on path for better lookup performance
CREATE INDEX IF NOT EXISTS idx_file_data_path ON file_data(path); 