-- Lets a rider file a report too (previously only buyer/farmer could be a
-- reporter, even though rider could already be a target), with two new
-- reasons specific to that flow (did the buyer receive the item or not).
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).

ALTER TABLE reports
  MODIFY COLUMN reporter_type ENUM('buyer', 'farmer', 'rider') NOT NULL DEFAULT 'buyer',
  MODIFY COLUMN reason ENUM('received_damaged', 'received_incorrect', 'did_not_receive',
      'did_not_pay', 'did_not_pickup', 'abusive_behavior', 'item_received',
      'item_not_received', 'others') NOT NULL;
