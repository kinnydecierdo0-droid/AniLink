-- Adds the reports table used by the buyer-to-admin report system: a buyer
-- can report the farmer or rider on one of their orders (received damaged,
-- received incorrect, did not receive, or others), reviewed by admin.
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).
--
-- target_id stores farmers.id or riders.id (matching how products.farmer_id
-- and orders.rider_id already reference those tables), never users.id.

CREATE TABLE IF NOT EXISTS reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    buyer_id INT NOT NULL,
    target_type ENUM('farmer', 'rider') NOT NULL,
    target_id INT NOT NULL,
    reason ENUM('received_damaged', 'received_incorrect', 'did_not_receive', 'others') NOT NULL,
    details TEXT NULL,
    status ENUM('open', 'reviewed', 'resolved') NOT NULL DEFAULT 'open',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE
);
