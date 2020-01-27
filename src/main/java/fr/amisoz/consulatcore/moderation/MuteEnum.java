package fr.amisoz.consulatcore.moderation;

import org.bukkit.Material;

public enum MuteEnum {

    FLOOD("Flood", Material.PAPER, 15*60L, "15m"),
    CAPS_LOCK("Majuscules abusives", Material.APPLE, 10*60L, "10m"),
    INSULTE("Langage incorrect", Material.ACACIA_SIGN, 30*60L, "30m"),
    PUB("Publicité", Material.EGG, 60*60L, "1h"),
    PROVOCATION("Provocation", Material.WHITE_WOOL, 15*60L, "15m"),
    RACISTE("Racisme", Material.WATER_BUCKET, 60*60*6L, "6h");

    private String sanctionName;
    private Material guiMaterial;
    private Long durationSanction;
    private String formatDuration;

    MuteEnum(String sanctionName, Material guiMaterial, Long durationSanction, String formatDuration) {
        this.sanctionName = sanctionName;
        this.guiMaterial = guiMaterial;
        this.durationSanction = durationSanction;
        this.formatDuration = formatDuration;
    }

    public String getSanctionName() {
        return sanctionName;
    }

    public Material getGuiMaterial() {
        return guiMaterial;
    }

    public Long getDurationSanction() {
        return durationSanction;
    }

    public String getFormatDuration() {
        return formatDuration;
    }
}
