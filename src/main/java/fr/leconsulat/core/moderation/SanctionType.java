package fr.leconsulat.core.moderation;

import org.bukkit.Material;

public enum SanctionType {
    
    MUTE(Material.PAPER),
    BAN(Material.BARRIER);
    
    Material material;
    
    SanctionType(Material material){
        this.material = material;
    }
    
    public Material getMaterial(){
        return material;
    }
}
