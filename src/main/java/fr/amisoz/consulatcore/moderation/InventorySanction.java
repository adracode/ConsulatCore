package fr.amisoz.consulatcore.moderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventorySanction {

    public static Inventory selectSanctionInventory(String targetName){
        Inventory inventory = Bukkit.createInventory(null, 3*9,ChatColor.GOLD + "§lSanction" + ChatColor.GRAY + " ↠ " + ChatColor.YELLOW + targetName);

        ItemStack banSanction = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemMeta banMeta = banSanction.getItemMeta();
        assert banMeta != null;
        banMeta.setDisplayName(ChatColor.RED + "Bannir");
        banSanction.setItemMeta(banMeta);

        ItemStack muteSanction = new ItemStack(Material.PAPER, 1);
        ItemMeta muteMeta = muteSanction.getItemMeta();
        assert muteMeta != null;
        muteMeta.setDisplayName(ChatColor.GOLD + "Mute");
        muteSanction.setItemMeta(muteMeta);

        inventory.setItem(11, banSanction);
        inventory.setItem(15, muteSanction);

        return inventory;
    }

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

    public static Inventory banInventory(String targetName){
        Inventory inventory = Bukkit.createInventory(null, 3*9,ChatColor.RED + "§lBannir" + ChatColor.GRAY + " ↠ " + ChatColor.DARK_RED + targetName);
        Arrays.stream(BanEnum.values()).forEach(motif -> {
            ItemStack itemStack = new ItemStack(motif.getGuiMaterial());
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(ChatColor.RED + motif.getSanctionName());
            itemMeta.setLore(Arrays.asList(ChatColor.RED + "Durée : " + ChatColor.DARK_RED + motif.getFormatDuration(), motif.name()));
            itemStack.setItemMeta(itemMeta);
            inventory.addItem(itemStack);
        });

        return inventory;
    }

}
