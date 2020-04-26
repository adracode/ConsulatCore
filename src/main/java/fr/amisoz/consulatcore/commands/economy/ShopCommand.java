package fr.amisoz.consulatcore.commands.economy;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ShopCommand extends ConsulatCommand {
    
    public ShopCommand(){
        super("/shop list|help", 0, Rank.JOUEUR);
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
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM shopinfo");
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    int x = resultSet.getInt("shop_x");
                    int y = resultSet.getInt("shop_y");
                    int z = resultSet.getInt("shop_z");
                    Block block = (new Location(Bukkit.getWorlds().get(0), x, y, z)).getBlock();
                    BlockData data = block.getBlockData();
                    if(data instanceof Directional){
                        Directional directional = (Directional)data;
                        Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
                        Chest chest = (Chest)blockBehind.getState();
                        Inventory inventory = chest.getInventory();
                        int number = 0;
                        for(ItemStack itemStacks : inventory.getContents()){
                            if(itemStacks != null){
                                number += itemStacks.getAmount();
                            }
                        }
                        
                        if(number == 0){
                            System.out.println("empty shop x" + x + ":y" + y + ":z" + z);
                        } else {
                            System.out.println(resultSet.getString("material") + " : " + number);
                        }
                        
                        PreparedStatement update = ConsulatAPI.getDatabase().prepareStatement("UPDATE shopinfo SET isEmpty = ? WHERE shop_x = ? AND shop_y = ? AND shop_z = ? AND owner_uuid = ? AND price = ?");
                        update.setBoolean(1, (number == 0));
                        update.setInt(2, x);
                        update.setInt(3, y);
                        update.setInt(4, z);
                        update.setString(5, resultSet.getString("owner_uuid"));
                        update.setDouble(6, resultSet.getDouble("price"));
                        update.executeUpdate();
                        update.close();
                    }
                }
                resultSet.close();
                preparedStatement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
            return;
        }
        switch(args[0].toLowerCase()){
            case "list":
                Inventory inventory = createShoplistInventory(sender.getPlayer(), 1);
                if(inventory != null)
                    sender.getPlayer().openInventory(inventory);
                break;
            case "help":
                ShopManager.getInstance().tutorial((SurvivalPlayer)sender);
                break;
        }
    }
    
    //Pas encore modifié, je ferai après
    public static Inventory createShoplistInventory(Player player, int page){
        Inventory list = Bukkit.createInventory(null, 54, "§cListe des Shops");
        Collection<Shop> shopCollection = ShopManager.getInstance().getShops();
        List<Shop> shops = new ArrayList<>(shopCollection.size());
        for(Shop shop : shopCollection){
            if(!shop.isEmpty()){
                shops.add(shop);
            }
        }
        if(shops.size() == 0){
            player.sendMessage(ChatColor.RED + "Il n'y a aucun shop.");
            return null;
        } else {
            int inventoryUsedSlot = 44;
            int start = (inventoryUsedSlot * page) - inventoryUsedSlot;
            int end = (inventoryUsedSlot * page) - 1;
            int size = shops.size();
            for(int i = start; i <= end; ++i){
                if(size >= (i + 1)){
                    Shop shopItem = shops.get(i);
                    int x = shopItem.getX();
                    int z = shopItem.getZ();
                    int y = shopItem.getY();
                    double price = shopItem.getPrice();
                    String owner = shopItem.getOwnerName();
                    ItemStack item = new ItemStack(shopItem.getItem());
                    ItemMeta meta = item.getItemMeta();
                    List<String> lores = new ArrayList<>();
                    lores.add(ChatColor.YELLOW + "Vendu par: " + ChatColor.RED + owner);
                    lores.add(ChatColor.YELLOW + "Prix unitaire: " + ChatColor.RED + price + ChatColor.YELLOW + "€.");
                    lores.add(ChatColor.YELLOW + "Coordonnées: " + ChatColor.YELLOW + "X: " + ChatColor.RED + x + ChatColor.YELLOW + " Y: " + ChatColor.RED + y + ChatColor.YELLOW + " Z: " + ChatColor.RED + z);
                    lores.add(ChatColor.YELLOW + "Téléportation pour: " + ChatColor.RED + "10" + ChatColor.YELLOW + "€.");
                    meta.setLore(lores);
                    item.setItemMeta(meta);
                    list.addItem(item);
                } else {
                    break;
                }
            }
            list.setItem(45, getCustomItem(Material.ARROW, ChatColor.RED + "Page précédente"));
            list.setItem(49, getCustomItem(Material.PAPER, ChatColor.RED + "Page: " + page));
            list.setItem(53, getCustomItem(Material.ARROW, ChatColor.RED + "Page suivante"));
            return list;
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
