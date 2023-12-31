package fr.leconsulat.core.guis.shop;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.DataPagedGui;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.shop.player.PlayerShop;
import fr.leconsulat.core.shop.player.ShopItemType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemFlag;

import java.util.Iterator;
import java.util.logging.Level;

public class ShopGui extends DataPagedGui<ShopItemType> {
    
    public ShopGui(ShopItemType itemType){
        super(itemType, "§4Shops §c(0)", 6,
                IGui.getItem("§ePage précédente", (6 - 1) * 9, Material.ARROW),
                IGui.getItem("§ePage suivante", 6 * 9 - 1, Material.ARROW));
        setDynamicItemsRange(0, 45);
        setTemplateItems(45, 53);
        setSort((item1, item2) -> {
            if(item1.getAttachedObject() == null || item2.getAttachedObject() == null){
                return 0;
            }
            return ((PlayerShop)item1.getAttachedObject()).compareTo((PlayerShop)item2.getAttachedObject());
        });
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable pageGui){
        if(getData().equals(ShopItemType.ALL)){
            pageGui.getGui().setName("§4Shops §8(§3" + (pageGui.getPage() + 1) + "§8)");
        } else {
            pageGui.getGui().setName("§4Shops §8(§3" + getData().toString() + "§8) (§3" + (pageGui.getPage() + 1) + "§8)");
        }
    }
    
    @Override
    public void onPageOpen(GuiOpenEvent event, Pageable pageGui){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        if(player.isInCombat()){
            player.sendMessage(Text.IN_COMBAT);
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable pageGui){
        switch(event.getSlot()){
            case 45:
                if(pageGui.getPage() <= 0){
                    return;
                }
                getPage(pageGui.getPage() - 1).getGui().open(event.getPlayer());
                break;
            case 53:
                if(pageGui.getPage() == getCurrentPage()){
                    return;
                }
                getPage(pageGui.getPage() + 1).getGui().open(event.getPlayer());
                break;
            default:
                PlayerShop shop = (PlayerShop)pageGui.getGui().getItem(event.getSlot()).getAttachedObject();
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
                            player.sendMessage(Text.SHOP_NOT_FOUND);
                            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Shop not found in list: " + getItem(event.getSlot()));
                            ConsulatAPI.getConsulatAPI().logFile("Shop not found in list: " + getItem(event.getSlot()));
                        }
                    } catch(NullPointerException e){
                        player.sendMessage(Text.ERROR);
                        return;
                    }
                    player.sendMessage(Text.TELEPORTATION(10));
                    player.removeMoney(10.0);
                } else {
                    player.sendMessage(Text.NOT_ENOUGH_MONEY(10));
                }
        }
    }
    
    public void addShop(PlayerShop shop){
        if(shop.isEmpty()){
            return;
        }
        GuiItem item = new GuiItem(shop.getItem(), 0);
        item.setDescription(GuiItem.getDescription(item, "", "§eVendu par: §c" + shop.getOwnerName(),
                "§ePrix unitaire: §c" + ConsulatCore.formatMoney(shop.getPrice()),
                "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                "§eTéléportation pour: §c" + ConsulatCore.formatMoney(10) + "."));
        item.setAttachedObject(shop);
        item.removeItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        addItem(item);
    }
    
    public void removeShop(PlayerShop shop){
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && shop.equals(item.getAttachedObject())){
                iterator.remove();
                return;
            }
        }
    }
    
    public void updateShop(PlayerShop shop){
        for(GuiItem item : this){
            if(item != null && shop.equals(item.getAttachedObject())){
                item.setDescription(GuiItem.getDescription(shop.getItem(), "", "§eVendu par: §c" + shop.getOwnerName(),
                        "§ePrix unitaire: §c" + ConsulatCore.formatMoney(shop.getPrice()),
                        "§eCoordonnées: X: §c" + shop.getX() + "§e Y: §c" + shop.getY() + "§e Z: §c" + shop.getZ(),
                        "§eTéléportation pour: §c" + ConsulatCore.formatMoney(10) + "."));
                update(item.getSlot());
                return;
            }
        }
    }
    
    public static class Container extends GuiContainer<ShopItemType> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("shop", this);
        }
        
        @Override
        public Datable<ShopItemType> createGui(ShopItemType itemType){
            return new ShopGui(itemType);
        }
    }
    
    
}
