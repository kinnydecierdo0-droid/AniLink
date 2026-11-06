-- Adds a stored reason to account disablement, plus two new moderation
-- flows: warnings (admin notes against an account, acknowledged at next
-- login) and appeals (a disabled user's request for the admin to
-- reconsider). Lets the admin act on a reported user's account directly
-- from the report detail dialog, and lets a disabled user see why and
-- appeal it instead of only getting a canned "contact the administrator".
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).

ALTER TABLE users
  ADD COLUMN disabled_reason VARCHAR(500) NULL AFTER is_active;

CREATE TABLE IF NOT EXISTS warnings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appeals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    status ENUM('pending', 'approved', 'denied') NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
