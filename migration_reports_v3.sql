-- Adds an optional proof-image attachment to a report (e.g. a photo of
-- damaged/incorrect goods), the same way payments already attach a receipt.
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).

ALTER TABLE reports
  ADD COLUMN proof_image VARCHAR(255) NULL AFTER details;
