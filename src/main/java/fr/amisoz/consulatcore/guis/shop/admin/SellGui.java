package fr.amisoz.consulatcore.guis.shop.admin;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.ShopManager;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.inventory.ItemStack;

public class SellGui extends AdminShopGui {
    
    public SellGui(AdminShop data){
        super(data, "Vendre");
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        ItemStack item = getData().getItem();
        
        GuiItem sell1 = (new GuiItem(getData().getItem(), ITEM_1_SLOT));
        sell1.setDisplayName("§eVendre 1");
        sell1.setDescription(GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice())));
        setItem(sell1);
        
        GuiItem sell16 = new GuiItem(getData().getItem(), ITEM_16_SLOT);
        sell16.setAmount(16);
        sell16.setDisplayName("§eVendre 16");
        sell16.setDescription(GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice() * 16)));
        setItem(sell16);
        
        GuiItem sell64 = new GuiItem(getData().getItem(), ITEM_64_SLOT);
        sell64.setAmount(64);
        sell64.setDisplayName("§eVendre 64");
        sell64.setDescription(GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice() * 64)));
        setItem(sell64);
        
        GuiItem allInventory = new GuiItem(getData().getItem(), ITEM_ALL_SLOT);
        allInventory.setAmount(64);
        allInventory.setDisplayName("§eVendre l'inventaire");
        setItem(allInventory);
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        ItemStack item = getData().getItem();
        int amountToSell = player.getSimilarItems(getData().getItem());
        if(amountToSell == 0){
            setDescriptionPlayer(ITEM_1_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§cTu n'as pas assez d'items à vendre"));
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§cTu n'as pas d'items à vendre"));
        } else {
            setDescriptionPlayer(ITEM_1_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player))));
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§7Vendre §e" + amountToSell, "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * amountToSell)));
        }
        if(amountToSell < 16){
            setDescriptionPlayer(ITEM_16_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§cTu n'as pas assez d'items à vendre"));
        } else {
            setDescriptionPlayer(ITEM_16_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * 16)));
        }
        if(amountToSell < 64){
            setDescriptionPlayer(ITEM_64_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§cTu n'as pas assez d'items à vendre"));
        } else {
            setDescriptionPlayer(ITEM_64_SLOT, event.getPlayer(), GuiItem.getDescription(item, "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * 64)));
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        int amount = -1;
        switch(event.getSlot()){
            //@formatter:off
            case ITEM_1_SLOT: amount = 1; break;
            case ITEM_16_SLOT: amount = 16; break;
            case ITEM_64_SLOT: amount = 64; break;
            case ITEM_ALL_SLOT: break;
            default: return;
            //@formatter:on
        }
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        AdminShop shop = getData();
        double sellPrice = getPrice(player);
        ItemStack toSell = shop.getItem();
        int numberToSell = player.getSimilarItems(toSell);
        if(amount == -1){
            amount = numberToSell;
        }
        if(numberToSell < amount || numberToSell == 0){
            player.sendMessage(Text.NO_ITEM_TO_SELL);
            player.getPlayer().closeInventory();
            return;
        }
        player.removeSimilarItems(toSell, amount);
        player.addMoney(sellPrice *= amount);
        player.sendMessage(ShopManager.getInstance().formatShopMessage(shop.getItem(), amount, sellPrice, ShopManager.ShopAction.SELL));
        refresh(player);
    }
    
    private double getPrice(SurvivalPlayer player){
        double sellPrice = getData().getPrice();
        switch(player.getRank()){
            case TOURISTE:
                sellPrice *= 1.12;
                break;
            case FINANCEUR:
                sellPrice *= 1.15;
                break;
            default:
                if(player.hasPower(Rank.MECENE)){
                    sellPrice *= 1.20;
                }
        }
        return sellPrice;
    }
}
