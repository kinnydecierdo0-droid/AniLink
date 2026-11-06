-- Adds the delivery fee column used by the buyer checkout and buy-now flows.
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).
--
-- total_amount stays the GRAND total (items + delivery fee), because every
-- existing dashboard tile, order list and report already sums that column.
-- The item subtotal is derived as total_amount - delivery_fee.

ALTER TABLE orders
  ADD COLUMN delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER total_amount;
