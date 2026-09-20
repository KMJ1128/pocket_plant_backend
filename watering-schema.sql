-- Pocket Plant 전체 초기화 스키마
-- 주의: 아래 스크립트는 기존 pocket_plant 테이블과 데이터를 모두 삭제한다.

CREATE DATABASE IF NOT EXISTS pocket_plant
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pocket_plant;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS watering_commands;
DROP TABLE IF EXISTS watering_settings;
DROP TABLE IF EXISTS plant_diary_photos;
DROP TABLE IF EXISTS plant_diary_moods;
DROP TABLE IF EXISTS plant_diary;
DROP TABLE IF EXISTS ai_chat_messages;
DROP TABLE IF EXISTS ai_chat_rooms;
DROP TABLE IF EXISTS board_images;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS sensor_data;
DROP TABLE IF EXISTS social_logins;
DROP TABLE IF EXISTS plants;
DROP TABLE IF EXISTS plant_data;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    profile_image VARCHAR(255) NULL,
    role VARCHAR(255) NULL,
    auth_version VARCHAR(36) NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    kakao_id VARCHAR(255) NULL,
    naver_id VARCHAR(255) NULL,
    login_type ENUM('GENERAL', 'KAKAO', 'NAVER') NOT NULL DEFAULT 'GENERAL',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_nickname (nickname),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_auth_version (auth_version),
    UNIQUE KEY uk_users_kakao_id (kakao_id),
    UNIQUE KEY uk_users_naver_id (naver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plant_data (
    cntnt_no BIGINT NOT NULL,
    plant_name VARCHAR(255) NULL,
    growh_tp VARCHAR(255) NULL,
    winter_temperature VARCHAR(255) NULL,
    humidity VARCHAR(255) NULL,
    water_cycle_spring VARCHAR(255) NULL,
    PRIMARY KEY (cntnt_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    character_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(255) NULL,
    adopt_date DATE NULL,
    age INT NULL,
    personality VARCHAR(255) NULL,
    mac_address VARCHAR(255) NULL,
    image_url VARCHAR(500) NULL,
    is_bookmarked BOOLEAN NOT NULL DEFAULT FALSE,
    intimacy BIGINT NULL DEFAULT 0,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_plants_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    KEY idx_plants_user_id (user_id),
    KEY idx_plants_mac_address (mac_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE social_logins (
    id INT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(255) NOT NULL,
    social_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(255) NULL,
    refresh_token VARCHAR(255) NULL,
    token_expiry VARCHAR(255) NULL,
    created_at VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_social_logins_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_social_logins_provider_social_id
        UNIQUE (provider, social_id),
    KEY idx_social_logins_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sensor_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    temperature FLOAT NULL,
    humidity FLOAT NULL,
    light FLOAT NULL,
    soil FLOAT NULL,
    reg_date DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sensor_data_plant
        FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE,
    KEY idx_sensor_data_plant_reg_date (plant_id, reg_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE watering_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    automatic_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mode VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    amount_ml INT NOT NULL DEFAULT 100,
    interval_days INT NOT NULL DEFAULT 7,
    soil_moisture_threshold FLOAT NOT NULL DEFAULT 30,
    minimum_interval_hours INT NOT NULL DEFAULT 24,
    pump_ml_per_second FLOAT NOT NULL DEFAULT 30,
    last_watered_at DATETIME(6) NULL,
    next_watering_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_watering_settings_plant (plant_id),
    CONSTRAINT fk_watering_settings_plant
        FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE,
    CONSTRAINT chk_watering_settings_mode CHECK (mode IN ('MANUAL', 'INTERVAL', 'SOIL')),
    CONSTRAINT chk_watering_settings_amount CHECK (amount_ml BETWEEN 10 AND 500),
    CONSTRAINT chk_watering_settings_interval CHECK (interval_days BETWEEN 1 AND 90),
    CONSTRAINT chk_watering_settings_soil CHECK (soil_moisture_threshold BETWEEN 5 AND 80),
    CONSTRAINT chk_watering_settings_cooldown CHECK (minimum_interval_hours BETWEEN 6 AND 720),
    CONSTRAINT chk_watering_settings_pump_rate CHECK (pump_ml_per_second BETWEEN 5 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE watering_commands (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    amount_ml INT NOT NULL,
    duration_ms INT NOT NULL,
    source VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    dispatched_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    result_message VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_watering_commands_plant
        FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE,
    CONSTRAINT chk_watering_commands_source CHECK (source IN ('MANUAL', 'INTERVAL', 'SOIL')),
    CONSTRAINT chk_watering_commands_status CHECK (status IN ('PENDING', 'DISPATCHED', 'COMPLETED', 'FAILED', 'EXPIRED')),
    CONSTRAINT chk_watering_commands_amount CHECK (amount_ml BETWEEN 10 AND 500),
    CONSTRAINT chk_watering_commands_duration CHECK (duration_ms BETWEEN 300 AND 30000),
    KEY idx_watering_command_plant_status (plant_id, status),
    KEY idx_watering_command_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plant_diary (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plant_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    day INT NULL,
    note VARCHAR(300) NOT NULL,
    temp VARCHAR(255) NULL,
    humidity VARCHAR(255) NULL,
    soil VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_plant_diary_plant
        FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE,
    KEY idx_plant_diary_plant_created (plant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plant_diary_moods (
    diary_id BIGINT NOT NULL,
    mood VARCHAR(255) NULL,
    CONSTRAINT fk_plant_diary_moods_diary
        FOREIGN KEY (diary_id) REFERENCES plant_diary(id) ON DELETE CASCADE,
    KEY idx_plant_diary_moods_diary (diary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plant_diary_photos (
    diary_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NULL,
    CONSTRAINT fk_plant_diary_photos_diary
        FOREIGN KEY (diary_id) REFERENCES plant_diary(id) ON DELETE CASCADE,
    KEY idx_plant_diary_photos_diary (diary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_chat_rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    plant_id BIGINT NOT NULL,
    title VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_chat_rooms_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_chat_rooms_plant
        FOREIGN KEY (plant_id) REFERENCES plants(id) ON DELETE CASCADE,
    KEY idx_ai_chat_rooms_user_id (user_id),
    KEY idx_ai_chat_rooms_plant_id (plant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NULL,
    sender ENUM('USER', 'ASSISTANT') NULL,
    content TEXT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_chat_messages_room
        FOREIGN KEY (room_id) REFERENCES ai_chat_rooms(id) ON DELETE CASCADE,
    KEY idx_ai_chat_messages_room_id_id (room_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE boards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    writer VARCHAR(255) NOT NULL,
    views INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_boards_category_created_at (category, created_at),
    KEY idx_boards_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE board_images (
    board_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    image_url VARCHAR(500) NULL,
    PRIMARY KEY (board_id, sort_order),
    CONSTRAINT fk_board_images_board
        FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    board_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    writer VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    parent_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_board
        FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    KEY idx_comments_board_created_at (board_id, created_at),
    KEY idx_comments_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
