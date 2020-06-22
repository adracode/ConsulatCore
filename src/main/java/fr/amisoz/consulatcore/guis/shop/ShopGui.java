package fr.amisoz.consulatcore.guis.shop;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.Shop;
import fr.amisoz.consulatcore.shop.ShopItemType;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.Iterator;
import java.util.logging.Level;

public class ShopGui extends GuiContainer<ShopItemType> {
    
    public ShopGui(){
        super(6);
        int lines = 6;
        setTemplate("§4Shops §c(0)",
                getItem("§ePage précédente", (lines - 1) * 9, Material.ARROW),
                getItem("§ePage suivante", lines * 9 - 1, Material.ARROW)
        );
        setMoveableItemsRange(0, 45);
        addGui(createGui(ShopItemType.ALL));
        setCreateOnOpen(false);
    }
    
    public void addShop(Shop shop, ShopItemType key){
        if(shop.isEmpty()){
            return;
        }
        GuiItem item = new GuiItem(shop.getItem(), 0);
        item.setDescription("§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + shop.getPrice() + "§e€.",
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                "§eTéléportation pour: §c10§e€.");
        item.setAttachedObject(shop);
        Gui<ShopItemType> pagedGui = getGui(key);
        if(pagedGui != null){
            pagedGui.addItem(item);
        }
    }
    
    public void removeShop(Shop shop, ShopItemType key){
        Gui<ShopItemType> pagedGui = getGui(key);
        if(pagedGui == null){
            return;
        }
        for(Iterator<GuiItem> iterator = pagedGui.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && shop.equals(item.getAttachedObject())){
                iterator.remove();
                return;
            }
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<ShopItemType> event){
        PagedGui<ShopItemType> gui = event.getPagedGui();
        Object key = event.getData();
        if(key.equals(ShopItemType.ALL)){
            gui.setName("§4Shops §8(§3" + (event.getPage() + 1) + "§8)");
        } else {
            gui.setName("§4Shops §8(§3" + key.toString() + "§8) (§3" + (event.getPage() + 1) + "§8)");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<ShopItemType> event){
        switch(event.getSlot()){
            case 45:
                if(event.getPage() <= 0){
                    return;
                }
                event.getGui().open(event.getPlayer(), event.getPage() - 1);
                break;
            case 53:
                if(event.getPage() == event.getGui().getCurrentPage()){
                    return;
                }
                event.getGui().open(event.getPlayer(), event.getPage() + 1);
                break;
            default:
                Shop shop = (Shop)event.getPagedGui().getItem(event.getSlot()).getAttachedObject();
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
                            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Shop not found in list: " + event.getPagedGui().getItem(event.getSlot()));
                            ConsulatAPI.getConsulatAPI().logFile("Shop not found in list: " + event.getPagedGui().getItem(event.getSlot()));
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
