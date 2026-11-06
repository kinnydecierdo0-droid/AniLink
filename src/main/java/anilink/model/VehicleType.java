package anilink.model;

/**
 * The delivery vehicles a buyer can choose for a "Delivery" order, each with
 * its own fixed fee. Single source of truth for both {@code CheckoutPanel}
 * and {@code BuyNowDialog}, which otherwise duplicate their delivery-fee
 * logic independently.
 */
public enum VehicleType {

    MOTORCYCLE("Motorcycle", 200.00),
    MINI_VAN("Mini Van", 500.00),
    WING_VAN("Wing Van", 1000.00);

    private final String label;
    private final double fee;

    VehicleType(String label, double fee) {
        this.label = label;
        this.fee = fee;
    }

    public String getLabel() {
        return label;
    }

    public double getFee() {
        return fee;
    }

    /** So a {@code JComboBox<VehicleType>} shows the label directly. */
    @Override
    public String toString() {
        return label;
    }

    /** Looks up a vehicle by its stored {@link #name()} (as persisted on an
     * order), returning {@code null} for an unrecognized or null value. */
    public static VehicleType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (VehicleType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
