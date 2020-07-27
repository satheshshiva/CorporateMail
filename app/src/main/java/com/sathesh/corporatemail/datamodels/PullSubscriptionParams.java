package com.sathesh.corporatemail.datamodels;

public class PullSubscriptionParams {

    private String subscriptionId;
    private String watermark;

    public PullSubscriptionParams(String subscriptionId, String watermark) {
        this.subscriptionId = subscriptionId;
        this.watermark = watermark;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

    @Override
    public String toString() {
        return "PullSubscriptionParams{" +
                "subscriptionId='" + subscriptionId + '\'' +
                ", watermark='" + watermark + '\'' +
                '}';
    }
}
