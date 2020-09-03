package fr.leconsulat.core.shop.admin.custom;

import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ASHome extends AdminShopCustom {
    
    public static final String TYPE = "ADMIN_CUSTOM_HOME";
    private static final ItemStack item = new ItemStack(Material.RED_BED);
    
    static{
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Home supplémentaire");
        item.setItemMeta(meta);
    }
    
    public ASHome(int x, int y, int z, double price){
        super(x, y, z, price);
    }
    
    public ASHome(long coords){
        super(coords);
    }
    
    public ASHome(long coords, double price){
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
        return "Acheter un home supplémentaire";
    }
    
    @Override
    public boolean canBuy(SurvivalPlayer player){
        return player.canBuyHome() < 1;
    }
    
    @Override
    public void onBuy(SurvivalPlayer player){
        player.incrementLimitHome();
        player.sendMessage(Text.BUY_HOME);
    }
}
