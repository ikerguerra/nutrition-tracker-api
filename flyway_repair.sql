-- Flyway Repair Script for V5 Migration Checksum Mismatch
-- Run this script on your production database to fix the deployment issue

-- Step 1: Check current state of Flyway schema history
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    checksum,
    installed_on,
    success
FROM flyway_schema_history 
ORDER BY installed_rank;

-- Step 2: Verify the problematic migration
SELECT 
    version,
    description,
    checksum,
    success
FROM flyway_schema_history 
WHERE version = '5';

-- Current state:
-- version: 5
-- checksum: -1719866606 (OLD - in database)
-- We need to update it to: -2019876065 (NEW - in current code)

-- Step 3: Update the checksum to match the local file
UPDATE flyway_schema_history 
SET checksum = -2019876065 
WHERE version = '5';

-- Step 4: Verify the fix
SELECT 
    version,
    description,
    checksum,
    success
FROM flyway_schema_history 
WHERE version = '5';

-- Expected output after fix:
-- version: 5
-- checksum: -2019876065 (UPDATED)
-- success: 1

-- Step 5: Check all migrations are in good state
SELECT 
    version,
    description,
    checksum,
    success
FROM flyway_schema_history 
ORDER BY installed_rank;

-- After running this script, redeploy your application
-- The Flyway validation should pass and the application should start successfully

-- ALTERNATIVE: If you prefer to disable validation temporarily
-- Add this environment variable in Railway:
-- FLYWAY_VALIDATE_ON_MIGRATE=false
