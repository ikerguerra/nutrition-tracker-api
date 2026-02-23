-- V14__add_gamification_fields.sql
-- Add xp and level columns to user_profiles table for US-9.10
DROP PROCEDURE IF EXISTS AddColumnIfNotExistsV14;
DELIMITER $$
CREATE PROCEDURE AddColumnIfNotExistsV14(
    IN dbName VARCHAR(255),
    IN tableName VARCHAR(255),
    IN colName VARCHAR(255),
    IN colDef VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = dbName
        AND TABLE_NAME = tableName
        AND COLUMN_NAME = colName
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

CALL AddColumnIfNotExistsV14(DATABASE(), 'user_profiles', 'xp', 'INT NOT NULL DEFAULT 0');
CALL AddColumnIfNotExistsV14(DATABASE(), 'user_profiles', 'level', 'INT NOT NULL DEFAULT 1');

DROP PROCEDURE AddColumnIfNotExistsV14;
