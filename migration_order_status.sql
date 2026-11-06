-- Reworks the orders.status lifecycle to match the new per-role workflow:
-- pending -> preparing -> (ready_for_pickup | handed_to_rider) -> [on_the_way] -> completed
-- 'confirmed' and the generic 'ready' are retired.
--
-- IMPORTANT: this widens the enum FIRST, then remaps old values, then
-- narrows it. Writing a not-yet-legal value (e.g. 'ready_for_pickup') while
-- the column still only allows the old list fails - under strict SQL mode
-- the UPDATE is rejected outright; under non-strict mode MySQL silently
-- truncates the value to '' instead. Either way, doing the remap before
-- widening the column corrupts the row. Running this file more than once is
-- harmless - the second run simply finds no 'ready'/'confirmed' rows left.

ALTER TABLE orders MODIFY status
    ENUM('pending', 'confirmed', 'preparing', 'ready', 'ready_for_pickup',
         'handed_to_rider', 'on_the_way', 'completed', 'cancelled')
    DEFAULT 'pending';

UPDATE orders SET status = 'ready_for_pickup' WHERE status = 'ready' AND delivery_method = 'pickup';
UPDATE orders SET status = 'handed_to_rider' WHERE status = 'ready' AND delivery_method = 'delivery';

ALTER TABLE orders MODIFY status
    ENUM('pending', 'preparing', 'ready_for_pickup', 'handed_to_rider', 'on_the_way', 'completed', 'cancelled')
    DEFAULT 'pending';
