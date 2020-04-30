package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ShopCommand extends ConsulatCommand {
    
    public ShopCommand(){
        super("shop", "/shop list|help", 0, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length == 0){
            sender.sendMessage(Text.PREFIX + "§cListe des commandes:");
            sender.sendMessage(Text.PREFIX + "§c- §e/shop list §cte permet de voir la liste des shops !");
            sender.sendMessage(Text.PREFIX + "§c- §e/shop help §cte permet de savoir comment créer un shop !");
            return;
        }
        if(args[0].equalsIgnoreCase("init") && sender.getName().equalsIgnoreCase("Elfas_")){
            return;
        }
        switch(args[0].toLowerCase()){
            case "list":
                if(!GuiManager.getInstance().getRootGui("shop").open(sender, 1)){
                    sender.sendMessage(Text.PREFIX + "§cIl n'y a aucun shop.");
                }
                break;
            case "help":
                ShopManager.getInstance().tutorial((SurvivalPlayer)sender);
                break;
        }
    }
    
    public static ItemStack getCustomItem(Material mat, String name){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if(name != null){
            Objects.requireNonNull(meta).setDisplayName(name);
        }
        item.setItemMeta(meta);
        return item;
    }
    
}
