-- Follow-up to migration_reports.sql: lets a farmer file a report against a
-- buyer too (previously only buyer -> farmer/rider was possible), and backs
-- the new 10-active-reports auto-disable feature.
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).
--
-- buyer_id assumed every reporter was a buyer - no longer true, so it is
-- renamed to reporter_id and paired with a new reporter_type column, mirroring
-- the existing target_id/target_type pattern (no FK on either, since each can
-- point to one of three different tables depending on its *_type sibling).
-- The table is empty at the time of writing, so there is no data to migrate.

ALTER TABLE reports
  DROP FOREIGN KEY reports_ibfk_2,
  CHANGE COLUMN buyer_id reporter_id INT NOT NULL,
  ADD COLUMN reporter_type ENUM('buyer', 'farmer') NOT NULL DEFAULT 'buyer' AFTER reporter_id,
  MODIFY COLUMN target_type ENUM('farmer', 'rider', 'buyer') NOT NULL,
  MODIFY COLUMN reason ENUM('received_damaged', 'received_incorrect', 'did_not_receive',
      'did_not_pay', 'did_not_pickup', 'abusive_behavior', 'others') NOT NULL;
