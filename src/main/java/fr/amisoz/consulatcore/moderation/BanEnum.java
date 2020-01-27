package fr.amisoz.consulatcore.moderation;

import org.bukkit.Material;

public enum BanEnum {

    SPEEDHACK("Speedhack", Material.SUGAR, 60*60*24*30L, "1 mois"),
    FLY("Fly", Material.FEATHER, 60*60*24*30L, "1 mois"),
    AUTOCLICK("Autoclick", Material.CLOCK, 60*60*24*30L, "1 mois"),
    FORCEFIELD("Forcefield", Material.DIAMOND_SWORD, 60*60*24*30L, "1 mois"),
    XRAY("X-ray", Material.DIAMOND_BLOCK, 60*60*24*30L, "1 mois"),
    COMPLICITE("Complicité de triche", Material.ACACIA_SIGN, 60*60*24*30L, "1 mois"),
    CHEAT("Cheat", Material.GOLDEN_APPLE, 60*60*24*30L, "1 mois"),
    PILLAGE("Tentative de pillage", Material.BARRIER, 60*60*24*7L, "1 semaine"),
    KILL("Tentative de meurtre", Material.LAVA_BUCKET,   60*60*24*7L, "1 semaine"),
    USEBUG("Use-bug", Material.DIAMOND, 60*60*24*3L, "3 jours"),
    ANTIJEU("Anti jeu", Material.CAKE, 60*60*24L, "1 jour"),
    CONTOURNE("Contournement de sanction", Material.RED_WOOL, 60*60*24L, "1 jour"),
    DC("Double compte", Material.EGG, 60*60*24*30L, "1 mois"),
    DUPLICATION("Duplication", Material.DIAMOND, 60*60*24*7L, "1 semaine"),
    OTHER("Banni", Material.LAVA_BUCKET, 60*60*24*7L, "1 semaine"),
    MENACE("Menace (DDos,Dox,...)", Material.SKELETON_SKULL, 60*60*24*7L, "1 semaine");

    private String sanctionName;
    private Material guiMaterial;
    private Long durationSanction;
    private String formatDuration;

    BanEnum(String sanctionName, Material guiMaterial, Long durationSanction, String formatDuration) {
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
