package fr.leconsulat.core.shop.admin.custom;

import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ASFly extends AdminShopCustom {
    
    public static final String TYPE = "ADMIN_CUSTOM_FLY";
    private static final ItemStack item = new ItemStack(Material.ELYTRA);
    
    static{
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Fly 5 min");
        item.setItemMeta(meta);
    }
    
    public ASFly(int x, int y, int z, double price){
        super(x, y, z, price);
    }
    
    public ASFly(long coords){
        super(coords);
    }
    
    public ASFly(long coords, double price){
        super(coords, price);
    }
    
    @Override
    public ItemStack getItem(){
        return item;
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
    
    @Override
    public String getTitle(){
        return "Acheter le fly (5 min)";
    }
    
    @Override
    public boolean canBuy(SurvivalPlayer player){
        return !player.hasFly();
    }
    
    @Override
    public void onBuy(SurvivalPlayer player){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "boutique fly5 " + player.getName());
    }
}
