package anilink.model;

import java.util.Date;

public class Report {

    public static final String TARGET_FARMER = "farmer";
    public static final String TARGET_RIDER = "rider";
    public static final String TARGET_BUYER = "buyer";

    public static final String REPORTER_BUYER = "buyer";
    public static final String REPORTER_FARMER = "farmer";
    public static final String REPORTER_RIDER = "rider";

    public static final String REASON_DAMAGED = "received_damaged";
    public static final String REASON_INCORRECT = "received_incorrect";
    public static final String REASON_NOT_RECEIVED = "did_not_receive";
    public static final String REASON_NOT_PAID = "did_not_pay";
    public static final String REASON_NOT_PICKED_UP = "did_not_pickup";
    public static final String REASON_ABUSIVE = "abusive_behavior";
    public static final String REASON_ITEM_RECEIVED = "item_received";
    public static final String REASON_ITEM_NOT_RECEIVED = "item_not_received";
    public static final String REASON_OTHERS = "others";

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_REVIEWED = "reviewed";
    public static final String STATUS_RESOLVED = "resolved";

    /** Accounts auto-disable once they reach this many active (open/reviewed) reports. */
    public static final int AUTO_DISABLE_THRESHOLD = 10;

    private int id;
    private int orderId;
    private int reporterId;
    private String reporterType;
    private String targetType;
    private int targetId;
    private String reason;
    private String details;
    private String proofImage;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    private String reporterName;
    private String targetName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getProofImage() {
        return proofImage;
    }

    public void setProofImage(String proofImage) {
        this.proofImage = proofImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** The filer's display name (a buyer or a farmer) - populated by the admin listing query only. */
    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    /** The reported party's display name - populated by the admin listing query only. */
    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
