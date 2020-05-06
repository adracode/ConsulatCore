package fr.amisoz.consulatcore.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventorySanction {

    public static Inventory muteInventory(String targetName){
        Inventory inventory = Bukkit.createInventory(null, 3*9,ChatColor.GOLD + "§lMute" + ChatColor.GRAY + " ↠ " + ChatColor.YELLOW + targetName);

        Arrays.stream(MuteEnum.values()).forEach(motif -> {
            ItemStack itemStack = new ItemStack(motif.getGuiMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.GOLD + motif.getSanctionName());
            itemMeta.setLore(Arrays.asList(ChatColor.GOLD + "Durée : " + ChatColor.YELLOW + motif.getFormatDuration(), motif.name()));
            itemStack.setItemMeta(itemMeta);
            inventory.addItem(itemStack);
        });
        return inventory;
    }


}
