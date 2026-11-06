-- Adds the delivery vehicle column used by the buyer checkout and buy-now
-- flows (Motorcycle/Mini Van/Wing Van, each with its own fixed fee).
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).
--
-- Stores the VehicleType enum's name() (e.g. "MOTORCYCLE"), not its label.
-- NULL for pickup orders, which never have a vehicle.

ALTER TABLE orders
  ADD COLUMN vehicle_type VARCHAR(20) NULL AFTER delivery_method;
