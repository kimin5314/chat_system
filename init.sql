-- ===================================================
-- Chat System Database Initialization Script
-- ===================================================
-- Created for the Spring Boot Chat System Project
-- This script creates all necessary tables and relationships
-- ===================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS chat_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE chat_system;

-- ===================================================
-- Table: user
-- ===================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `display_name` VARCHAR(100),
    `avatar_url` TEXT,
    `public_key` TEXT,
    `e2ee_enabled` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX `idx_username` (`username`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_e2ee_enabled` (`e2ee_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Table: tokens
-- ===================================================
CREATE TABLE IF NOT EXISTS `tokens` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `token` TEXT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL,
    
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_token_expires` (`expires_at`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Table: contacts
-- ===================================================
CREATE TABLE IF NOT EXISTS `contacts` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `contact_id` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'pending',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`contact_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_contact_relationship` (`user_id`, `contact_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_contact_id` (`contact_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Table: message
-- ===================================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `sender_id` INT NOT NULL,
    `receiver_id` INT NOT NULL,
    `content` TEXT,
    `message_type` ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM', 'ENCRYPTED') NOT NULL DEFAULT 'TEXT',
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `encrypted_aes_key` TEXT,
    `iv` VARCHAR(255),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_conversation` (`sender_id`, `receiver_id`),
    INDEX `idx_conversation_reverse` (`receiver_id`, `sender_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_message_type` (`message_type`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Table: file_storage
-- ===================================================
CREATE TABLE IF NOT EXISTS `file_storage` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `file_name` TEXT,
    `file_url` TEXT NOT NULL,
    `file_type` ENUM('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT', 'OTHER') NOT NULL,
    `file_size` BIGINT DEFAULT 0,
    `uploaded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_file_type` (`file_type`),
    INDEX `idx_uploaded_at` (`uploaded_at`),
    INDEX `idx_file_size` (`file_size`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Table: file_permission
-- ===================================================
CREATE TABLE IF NOT EXISTS `file_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `file_id` BIGINT NOT NULL,
    `user_id` INT NOT NULL,
    `permission` ENUM('READ', 'DELETE', 'SHARE') NOT NULL,
    
    FOREIGN KEY (`file_id`) REFERENCES `file_storage`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_file_user_permission` (`file_id`, `user_id`, `permission`),
    INDEX `idx_file_id` (`file_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_permission` (`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================
-- Create Views for Common Queries
-- ===================================================

-- View: User contacts with user details
CREATE OR REPLACE VIEW `user_contacts_view` AS
SELECT 
    c.user_id,
    c.contact_id,
    c.status,
    c.created_at as friendship_created_at,
    u.username as contact_username,
    u.display_name as contact_display_name,
    u.avatar_url as contact_avatar_url,
    u.created_at as contact_created_at
FROM `contacts` c
JOIN `user` u ON c.contact_id = u.id
WHERE c.status = 'accepted';

-- View: Conversation list with last message
CREATE OR REPLACE VIEW `conversation_list_view` AS
SELECT 
    LEAST(sender_id, receiver_id) as user1_id,
    GREATEST(sender_id, receiver_id) as user2_id,
    MAX(created_at) as last_message_time,
    (SELECT content FROM message m2 
     WHERE (m2.sender_id = LEAST(m.sender_id, m.receiver_id) AND m2.receiver_id = GREATEST(m.sender_id, m.receiver_id))
        OR (m2.sender_id = GREATEST(m.sender_id, m.receiver_id) AND m2.receiver_id = LEAST(m.sender_id, m.receiver_id))
     ORDER BY m2.created_at DESC LIMIT 1) as last_message_content,
    (SELECT COUNT(*) FROM message m3 
     WHERE m3.receiver_id = GREATEST(m.sender_id, m.receiver_id) 
       AND m3.sender_id = LEAST(m.sender_id, m.receiver_id)
       AND m3.is_read = FALSE) as unread_count
FROM `message` m
GROUP BY LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id);

-- ===================================================
-- Create Stored Procedures
-- ===================================================

DELIMITER //

-- Procedure: Add friend relationship (bidirectional)
CREATE PROCEDURE AddFriendship(
    IN p_user_id INT,
    IN p_contact_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Insert friendship from user to contact
    INSERT INTO `contacts` (`user_id`, `contact_id`, `status`) 
    VALUES (p_user_id, p_contact_id, 'pending')
    ON DUPLICATE KEY UPDATE `status` = 'pending';
    
    COMMIT;
END //

-- Procedure: Accept friend request (make bidirectional)
CREATE PROCEDURE AcceptFriendship(
    IN p_user_id INT,
    IN p_contact_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Update the original request to accepted
    UPDATE `contacts` 
    SET `status` = 'accepted' 
    WHERE `user_id` = p_contact_id AND `contact_id` = p_user_id;
    
    -- Create the reverse relationship
    INSERT INTO `contacts` (`user_id`, `contact_id`, `status`) 
    VALUES (p_user_id, p_contact_id, 'accepted')
    ON DUPLICATE KEY UPDATE `status` = 'accepted';
    
    COMMIT;
END //

-- Procedure: Get unread message count for a user
CREATE PROCEDURE GetUnreadMessageCount(
    IN p_user_id INT,
    OUT p_unread_count INT
)
BEGIN
    SELECT COUNT(*) INTO p_unread_count
    FROM `message`
    WHERE `receiver_id` = p_user_id AND `is_read` = FALSE;
END //

DELIMITER ;

-- ===================================================
-- Create Triggers
-- ===================================================

DELIMITER //

-- Trigger: Update message updated_at on read status change
CREATE TRIGGER message_update_trigger
    BEFORE UPDATE ON `message`
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //

-- Trigger: Prevent self-friendship
CREATE TRIGGER prevent_self_friendship
    BEFORE INSERT ON `contacts`
    FOR EACH ROW
BEGIN
    IF NEW.user_id = NEW.contact_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Users cannot add themselves as contacts';
    END IF;
END //

DELIMITER ;

-- ===================================================
-- Indexes for Performance Optimization
-- ===================================================

-- Additional composite indexes for better query performance
CREATE INDEX `idx_message_conversation_time` ON `message` (`sender_id`, `receiver_id`, `created_at`);
CREATE INDEX `idx_message_unread_receiver` ON `message` (`receiver_id`, `is_read`, `created_at`);
CREATE INDEX `idx_contacts_user_status` ON `contacts` (`user_id`, `status`);
CREATE INDEX `idx_file_storage_user_type` ON `file_storage` (`user_id`, `file_type`);

-- ===================================================
-- Grant Permissions (Adjust username/password as needed)
-- ===================================================

-- Create application user (uncomment and modify as needed)
-- CREATE USER IF NOT EXISTS 'chat_app'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON chat_system.* TO 'chat_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ===================================================
-- Database Setup Complete
-- ===================================================

-- Display table information
SELECT 'Database initialization completed successfully!' as Status;
SHOW TABLES;
