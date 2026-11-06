package anilink.model;

/**
 * A reportable party on a specific order - either a farmer behind one of its
 * line items or the rider assigned to deliver it. Carries the id/type pair
 * that gets written to {@code reports.target_type}/{@code reports.target_id}.
 */
public class ReportTarget {

    private final String type;
    private final int id;
    private final String name;

    public ReportTarget(String type, int id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String label = Report.TARGET_RIDER.equals(type) ? "Rider"
                : Report.TARGET_BUYER.equals(type) ? "Buyer" : "Farmer";
        return label + ": " + name;
    }
}
