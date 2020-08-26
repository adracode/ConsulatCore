package fr.amisoz.consulatcore.commands.economy;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.amisoz.consulatcore.shop.admin.AdminShopBuy;
import fr.amisoz.consulatcore.shop.admin.AdminShopSell;
import fr.amisoz.consulatcore.shop.admin.custom.ASFly;
import fr.amisoz.consulatcore.shop.admin.custom.ASHome;
import fr.amisoz.consulatcore.shop.admin.custom.ASSlotShop;
import fr.amisoz.consulatcore.shop.admin.custom.ASTouriste;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AdminShopCommand extends ConsulatCommand {
    
    private static final Set<String> SUB_COMMAND = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("sell", "buy", "home", "fly", "touriste", "shop")));
    
    public AdminShopCommand(){
        super(ConsulatCore.getInstance(), "adminshop");
        List<ArgumentBuilder<Object, ?>> args = new ArrayList<>(SUB_COMMAND.size());
        for(String arg : SUB_COMMAND){
            args.add(LiteralArgumentBuilder.literal(arg)
                    .then(RequiredArgumentBuilder.argument("prix", DoubleArgumentType.doubleArg(0, 1_000_000))));
        }
        //noinspection unchecked
        setDescription("Place un shop au dessus du bloc visé, avec l'item concerné en main").
                setUsage("/adminshop buy <prix> - Acheter des items\n" +
                        "/adminshop sell <prix> - Vendre des items\n" +
                        "/adminshop fly <prix> - Acheter un fly\n" +
                        "/adminshop home <prix> - Acheter un home\n" +
                        "/adminshop shop <prix> - Acheter un slot de shop\n" +
                        "/adminshop touriste <prix> - Acheter le grade Touriste").
                setArgsMin(2).
                setRank(Rank.RESPONSABLE).
                suggest(args.toArray(new ArgumentBuilder[0]));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        Player bukkitPlayer = sender.getPlayer();
        if(bukkitPlayer.getWorld() != ConsulatCore.getInstance().getOverworld()){
            sender.sendMessage(Text.DIMENSION_SHOP);
            return;
        }
        Block shopBlock = bukkitPlayer.getTargetBlock(5);
        if(shopBlock != null){
            shopBlock = shopBlock.getRelative(BlockFace.UP);
        } else {
            sender.sendMessage(Text.NO_TARGETED_BLOCK);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch(NumberFormatException e){
            sender.sendMessage(Text.INVALID_NUMBER);
            return;
        }
        if(price <= 0 || price >= 1_000_000){
            sender.sendMessage(Text.INVALID_MONEY);
            return;
        }
        if(!SUB_COMMAND.contains(args[0])){
            sender.sendMessage(Text.COMMAND_USAGE(this));
            return;
        }
        shopBlock.setType(Material.CHEST);
        Directional directional = (Directional)shopBlock.getBlockData();
        directional.setFacing(bukkitPlayer.getFacing().getOppositeFace());
        shopBlock.setBlockData(directional);
        ItemStack item;
        if(!args[0].equals("sell") && !args[0].equals("buy")){
            item = new ItemStack(Material.DIRT);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList("§4§kD§c§ke §6§kr§e§ki§2§ke§a§kn §b§km§3§ko§1§kn§9§ks§d§ki§5§ke§f§ku§7§kr§8§k."));
            item.setItemMeta(meta);
        } else {
            item = bukkitPlayer.getInventory().getItemInMainHand().clone();
            item.setAmount(1);
            if(item.getType() == Material.AIR){
                sender.sendMessage(Text.NO_ITEM_IN_HAND);
                return;
            }
        }
        ((Chest)shopBlock.getState()).getBlockInventory().setItem(0, item);
        AdminShop shop;
        switch(args[0]){
            case "sell":
                shop = new AdminShopSell(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "buy":
                shop = new AdminShopBuy(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "fly":
                shop = new ASFly(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "home":
                shop = new ASHome(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "touriste":
                shop = new ASTouriste(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            case "shop":
                shop = new ASSlotShop(shopBlock.getX(), shopBlock.getY(), shopBlock.getZ(), price);
                break;
            default:
                return;
        }
        shop.createGui();
        ShopManager.getInstance().addAdminShop(shop);
    }
}
