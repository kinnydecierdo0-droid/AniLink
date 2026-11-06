-- Splits a delivery order's G-Cash payment into two separately-approved
-- portions: the delivery fee (admin approves, in this new table) and the
-- product subtotal (farmer approves, still the existing `payments` table -
-- that table's schema is untouched; only its audience changes for delivery
-- orders going forward).
-- Run once against the anilink database (phpMyAdmin > SQL tab, or the mysql CLI).

CREATE TABLE IF NOT EXISTS delivery_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    reference_no VARCHAR(100) NULL,
    proof_image VARCHAR(255) NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'verified', 'rejected') NOT NULL DEFAULT 'pending',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_delivery_payment_order (order_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Backfill: every existing delivery+G-Cash order gets a delivery_payments row
-- mirroring its existing (pre-split) payments row, so nothing already
-- verified/in-progress gets newly blocked by the dual-approval gate this
-- feature adds. An order that's already `completed` is forced to 'verified'
-- regardless of its old payment status, since re-litigating a finished
-- order's payment history would only produce a visible regression with no
-- real benefit - it already succeeded under the old single-payment rules.
INSERT INTO delivery_payments (order_id, reference_no, proof_image, amount, status, submitted_at, reviewed_at)
SELECT o.id, p.reference_no, p.proof_image, o.delivery_fee,
    CASE WHEN o.status = 'completed' THEN 'verified' ELSE COALESCE(p.status, 'pending') END,
    COALESCE(p.submitted_at, o.created_at),
    CASE WHEN o.status = 'completed' THEN COALESCE(p.reviewed_at, CURRENT_TIMESTAMP) ELSE p.reviewed_at END
FROM orders o
LEFT JOIN payments p ON p.order_id = o.id
WHERE o.delivery_method = 'delivery' AND o.payment_method = 'Online Payment';
