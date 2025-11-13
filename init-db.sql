-- Database initialization script for Restaurant Billing System
-- This script creates the necessary extensions and initial data

-- Create UUID extension if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create database if not exists (this will be handled by docker-compose)
-- The database is created by the POSTGRES_DB environment variable

-- Grant permissions to the application user
GRANT ALL PRIVILEGES ON DATABASE restaurant_billing TO adsuser;
GRANT ALL ON SCHEMA public TO adsuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adsuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adsuser;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO adsuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO adsuser;