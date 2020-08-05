package fr.amisoz.consulatcore.commands.economy;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.amisoz.consulatcore.shop.admin.AdminShopBuy;
import fr.amisoz.consulatcore.shop.admin.AdminShopSell;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AdminShopCommand extends ConsulatCommand {
    
    private static final Set<String> SUB_COMMAND = new HashSet<>(Arrays.asList("sell", "buy"));
    
    public AdminShopCommand(){
        super("adminshop", "/adminshop [sell|buy] <prix>", 2, Rank.RESPONSABLE);
        suggest(false,
                LiteralArgumentBuilder.literal("sell")
                        .then(RequiredArgumentBuilder.argument("prix", DoubleArgumentType.doubleArg(0, 1_000_000))),
                LiteralArgumentBuilder.literal("buy")
                        .then(RequiredArgumentBuilder.argument("prix", DoubleArgumentType.doubleArg(0, 1_000_000))));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        Player bukkitPlayer = sender.getPlayer();
        if(bukkitPlayer.getWorld() != Bukkit.getWorlds().get(0)){
            sender.sendMessage("§cUn Shop doit être placé dans l'overworld.");
            return;
        }
        ItemStack item = bukkitPlayer.getInventory().getItemInMainHand().clone();
        item.setAmount(1);
        if(item.getType() == Material.AIR){
            sender.sendMessage("§cMerci de tenir l'item concerné en main.");
            return;
        }
        Block shopBlock = bukkitPlayer.getTargetBlock(5);
        if(shopBlock != null){
            shopBlock = shopBlock.getRelative(BlockFace.UP);
        } else {
            sender.sendMessage("§cUn bloc doit être visé pour créer le shop.");
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            sender.sendMessage("§cCe nombre n'est pas valide.");
            return;
        }
        if(price <= 0 || price >= 1_000_000){
            sender.sendMessage("§cCe prix n'est pas valide.");
            return;
        }
        if(!SUB_COMMAND.contains(args[0])){
            sender.sendMessage("§c" + getUsage());
            return;
        }
        shopBlock.setType(Material.CHEST);
        Directional directional = (Directional)shopBlock.getBlockData();
        directional.setFacing(bukkitPlayer.getFacing().getOppositeFace());
        shopBlock.setBlockData(directional);
        ((Chest)shopBlock.getState()).getBlockInventory().setItem(0, item);
        AdminShop shop;
        switch(args[0]){
            case "sell":
                shop = new AdminShopSell(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "buy":
                shop = new AdminShopBuy(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            default:
                return;
        }
        shop.createGui();
        ShopManager.getInstance().addAdminShop(shop);
    }
}
