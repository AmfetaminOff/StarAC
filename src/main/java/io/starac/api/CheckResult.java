package io.starac.api;

public class CheckResult {

    private final boolean flagged;
    private final double confidence;
    private final String reason;

    public CheckResult(boolean flagged, double confidence, String reason) {
        this.flagged = flagged;
        this.confidence = confidence;
        this.reason = reason;
    }

    public boolean flagged() {
        return flagged;
    }

    public double confidence() {
        return confidence;
    }

    public String reason() {
        return reason;
    }

    public static CheckResult pass() {
        return new CheckResult(false,0,null);
    }

    public static CheckResult fail(double confidence,String reason){
        return new CheckResult(true,confidence,reason);
    }

}