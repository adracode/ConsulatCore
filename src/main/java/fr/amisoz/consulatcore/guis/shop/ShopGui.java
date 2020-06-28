package fr.amisoz.consulatcore.guis.shop;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopItemType;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.DataPagedGui;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.Iterator;
import java.util.logging.Level;

public class ShopGui extends DataPagedGui<ShopItemType> {
    
    public ShopGui(ShopItemType itemType){
        super(itemType, "§4Shops §c(0)", 6,
                IGui.getItem("§ePage précédente", (6 - 1) * 9, Material.ARROW),
                IGui.getItem("§ePage suivante", 6 * 9 - 1, Material.ARROW));
        setDynamicItemsRange(0, 45);
    }
    
    public void addShop(Shop shop){
        if(shop.isEmpty()){
            return;
        }
        GuiItem item = new GuiItem(shop.getItem(), 0);
        item.setDescription("§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + shop.getPrice() + "§e€.",
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                "§eTéléportation pour: §c10§e€.");
        item.setAttachedObject(shop);
        addItem(item);
    }
    
    public void removeShop(Shop shop){
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && shop.equals(item.getAttachedObject())){
                iterator.remove();
                return;
            }
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable pageGui){
        if(getData().equals(ShopItemType.ALL)){
            pageGui.setName("§4Shops §8(§3" + (pageGui.getPage() + 1) + "§8)");
        } else {
            pageGui.setName("§4Shops §8(§3" + getData().toString() + "§8) (§3" + (pageGui.getPage() + 1) + "§8)");
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable pageGui){
        switch(event.getSlot()){
            case 45:
                if(pageGui.getPage() <= 0){
                    return;
                }
                getPage(pageGui.getPage() - 1).open(event.getPlayer());
                break;
            case 53:
                if(pageGui.getPage() == getCurrentPage()){
                    return;
                }
                getPage(pageGui.getPage() + 1).open(event.getPlayer());
                break;
            default:
                Shop shop = (Shop)pageGui.getItem(event.getSlot()).getAttachedObject();
                SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
                player.getPlayer().closeInventory();
                if(player.hasMoney(10.0)){
                    try {
                        if(shop != null){
                            Sign sign = shop.getSign();
                            Location shopLocation = shop.getLocation();
                            if(sign == null){
                                player.getPlayer().teleportAsync(shopLocation.clone().add(0, 1, 0));
                            } else {
                                Location block = sign.getLocation().clone().add(0.5, 0, 0.5);
                                block.setDirection(block.toBlockLocation().subtract(shopLocation).multiply(-1).toVector());
                                if(block.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR){
                                    block.add(0, -1, 0);
                                }
                                player.getPlayer().teleportAsync(block);
                            }
                        } else {
                            player.sendMessage(Text.PREFIX + "§cCe shop n'a pas été trouvé");
                            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Shop not found in list: " + getItem(event.getSlot()));
                            ConsulatAPI.getConsulatAPI().logFile("Shop not found in list: " + getItem(event.getSlot()));
                        }
                    } catch(NullPointerException e){
                        player.sendMessage("Erreur lors de la téléportation");
                        return;
                    }
                    player.sendMessage(ChatColor.YELLOW + "Téléportation réussie pour " + ChatColor.RED + "10.0" + ChatColor.YELLOW + "€.");
                    player.removeMoney(10.0);
                } else {
                    player.sendMessage(Text.PREFIX + "§cVous n'avez pas assez d'argent.");
                }
        }
    }
}
