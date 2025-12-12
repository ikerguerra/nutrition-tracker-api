-- Add daily_weight column to daily_logs table
ALTER TABLE daily_logs 
ADD COLUMN daily_weight DECIMAL(5, 2) NULL AFTER total_fats;
