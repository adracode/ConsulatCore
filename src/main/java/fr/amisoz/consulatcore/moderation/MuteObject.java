package fr.amisoz.consulatcore.moderation;

public class MuteObject {

    private String reason;
    private String endDate;

    public MuteObject(String reason, String endDate) {
        this.reason = reason;
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public String getEndDate() {
        return endDate;
    }
}
