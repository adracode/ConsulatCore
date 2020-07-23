package fr.amisoz.consulatcore.chunks.scan;

import org.bukkit.Material;

@FunctionalInterface
public interface ScanAction {
    
    void action(int x, int y, int z, Material type);
    
}
