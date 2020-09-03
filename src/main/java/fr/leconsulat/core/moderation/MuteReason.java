package fr.leconsulat.core.moderation;

import org.bukkit.Material;

public enum MuteReason {
    
    FLOOD("Flood", Material.PAPER, 15 * 60L, "15 minutes"),
    SPAM("Spam", Material.MAP, 30 * 60L, "30 minutes"),
    CAPS_LOCK("Majuscules abusives", Material.APPLE, 10 * 60L, "10 minutes"),
    INSULTE("Langage incorrect", Material.ACACIA_SIGN, 30 * 60L, "30 minutes"),
    ARROGANCE_IRRESPECT("Arrogance/Irrespect", Material.SKELETON_SKULL, 30 * 60L, "30 minutes"),
    INSULTE_STAFF("Insulte staff", Material.BARRIER, 60 * 60L, "1 heure"),
    REPORT_ABUSE("Abus de /report", Material.BLACK_TERRACOTTA, 60 * 60L, "1 heure"),
    PUB("Publicité", Material.EGG, 60 * 60L, "1 heure"),
    PROVOCATION("Provocation", Material.WHITE_WOOL, 15 * 60L, "15 minutes"),
    RACISME("Racisme", Material.WATER_BUCKET, 60 * 60 * 6L, "6 heures"),
    HOMOPHOBIE("Homophobie", Material.LAVA_BUCKET, 60 * 60 * 6L, "6 heures");
    
    private String sanctionName;
    private Material guiMaterial;
    private long durationSanction;
    private String formatDuration;
    
    MuteReason(String sanctionName, Material guiMaterial, long durationSanction, String formatDuration){
        this.sanctionName = sanctionName;
        this.guiMaterial = guiMaterial;
        this.durationSanction = durationSanction;
        this.formatDuration = formatDuration;
    }
    
    public String getSanctionName(){
        return sanctionName;
    }
    
    public Material getGuiMaterial(){
        return guiMaterial;
    }
    
    public long getDurationSanction(){
        return durationSanction;
    }
    
    public String getFormatDuration(){
        return formatDuration;
    }
}
