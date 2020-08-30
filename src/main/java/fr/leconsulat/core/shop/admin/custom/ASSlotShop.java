package fr.leconsulat.core.shop.admin.custom;

import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ASSlotShop extends AdminShopCustom {
    
    public static final String TYPE = "ADMIN_CUSTOM_SLOT_SHOP";
    private static final ItemStack item = new ItemStack(Material.CHEST);
    
    static{
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Slot de shop");
        item.setItemMeta(meta);
    }
    
    public ASSlotShop(int x, int y, int z, double price){
        super(x, y, z, price);
    }
    
    public ASSlotShop(long coords){
        super(coords);
    }
    
    public ASSlotShop(long coords, double price){
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
        return "Acheter un slot de shop";
    }
    
    @Override
    public boolean canBuy(SurvivalPlayer player){
        return player.canBuySlotShop() < 1;
    }
    
    @Override
    public void onBuy(SurvivalPlayer player){
        player.incrementSlotShopHome();
        player.sendMessage(Text.BUY_SLOT_SHOP);
    }
}
