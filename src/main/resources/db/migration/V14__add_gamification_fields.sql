-- V14__add_gamification_fields.sql
-- Add xp and level columns to user_profiles table for US-9.10

ALTER TABLE user_profiles 
ADD COLUMN xp INT NOT NULL DEFAULT 0,
ADD COLUMN level INT NOT NULL DEFAULT 1;
