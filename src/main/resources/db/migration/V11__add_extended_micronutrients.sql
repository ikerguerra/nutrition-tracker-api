-- Add new micronutrients columns to nutritional_info table
DROP PROCEDURE IF EXISTS AddColumnIfNotExistsV11;
DELIMITER $$
CREATE PROCEDURE AddColumnIfNotExistsV11(
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

CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'magnesium', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'zinc', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_k', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_b1', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_b2', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_b3', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_b6', 'DECIMAL(10, 2)');
CALL AddColumnIfNotExistsV11(DATABASE(), 'nutritional_info', 'vitamin_b9', 'DECIMAL(10, 2)');

DROP PROCEDURE AddColumnIfNotExistsV11;
