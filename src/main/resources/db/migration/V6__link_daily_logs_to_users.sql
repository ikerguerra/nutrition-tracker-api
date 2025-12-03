-- Add foreign key constraint to daily_logs table
-- This links existing daily logs to users
ALTER TABLE daily_logs 
ADD CONSTRAINT fk_daily_logs_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index for better query performance
CREATE INDEX idx_daily_logs_user_date ON daily_logs(user_id, date);
