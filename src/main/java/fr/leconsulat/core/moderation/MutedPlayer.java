package fr.leconsulat.core.moderation;

public class MutedPlayer {
    
    private String reason;
    private String endDate;
    
    public MutedPlayer(String reason, String endDate){
        this.reason = reason;
        this.endDate = endDate;
    }
    
    public String getReason(){
        return reason;
    }
    
    public String getEndDate(){
        return endDate;
    }
}
