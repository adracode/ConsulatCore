package fr.leconsulat.core.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;

public class ItemUtils {
    
    public static boolean areItemEquals(ItemStack item1, ItemStack item2){
        if(item1 == null && item2 != null){
            return false;
        }
        if(item2 == null && item1 != null){
            return false;
        }
        if(item2 == null){
            return true;
        }
        if(item1.getType() != item2.getType()){
            return false;
        }
        if(item1.getItemMeta().equals(item2.getItemMeta())){
            return true;
        }
        if(item1.getItemMeta() instanceof SkullMeta && item2.getItemMeta() instanceof SkullMeta){
            return Objects.equals(((SkullMeta)item1.getItemMeta()).getOwner(), ((SkullMeta)item2.getItemMeta()).getOwner());
        }
        return false;
    }
    
}
