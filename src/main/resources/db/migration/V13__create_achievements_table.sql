-- V13: Create achievements table
CREATE TABLE IF NOT EXISTS achievements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    target INT NOT NULL DEFAULT 1,
    unlocked_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_achievement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_achievement_user_type UNIQUE (user_id, type),
    INDEX idx_achievements_user_id (user_id)
);
