package fr.amisoz.consulatcore.shop.admin.custom;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ASTouriste extends AdminShopCustom {
    
    public static final String TYPE = "ADMIN_CUSTOM_TOURISTE";
    private static final ItemStack item = new ItemStack(Material.BEACON);
    
    static {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Touriste");
        item.setItemMeta(meta);
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
    
    public ASTouriste(int x, int y, int z, double price){
        super(x, y, z, price);
    }
    
    public ASTouriste(long coords){
        super(coords);
    }
    
    public ASTouriste(long coords, double price){
        super(coords, price);
    }
    
    @Override
    public ItemStack getItem(){
        return item;
    }
    
    @Override
    public String getTitle(){
        return "Acheter le grade Touriste";
    }
    
    @Override
    public boolean canBuy(SurvivalPlayer player){
        return player.getRank() == Rank.JOUEUR;
    }
    
    @Override
    public void onBuy(SurvivalPlayer player){
        player.setRank(Rank.TOURISTE);
        player.sendMessage(Text.NOW_TOURISTE);
    }
}
