package fr.amisoz.consulatcore.guis.shop.admin;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.inventory.ItemStack;

public class SellGui extends AdminShopGui {
    
    public SellGui(AdminShop data){
        super(data, "Vendre");
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        GuiItem buy1 = IGui.getItem("§eVendre 1", 37, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice()));
        GuiItem buy16 = IGui.getItem("§eVendre 16", 39, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice()));
        buy16.setAmount(16);
        GuiItem buy64 = IGui.getItem("§eVendre 64", 41, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice()));
        buy64.setAmount(64);
        GuiItem fillInventory = IGui.getItem("§eVendre l'inventaire", 43, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice()));
        fillInventory.setAmount(64);
        setItem(buy1);
        setItem(buy16);
        setItem(buy64);
        setItem(fillInventory);
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        int amountToSell = player.getSimilarItems(getData().getItem());
        if(amountToSell == 0){
            setDescriptionPlayer(ITEM_1_SLOT, event.getPlayer(), "", "§cTu n'as pas assez d'items à vendre");
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), "", "§cTu n'as pas d'items à vendre");
        } else {
            setDescriptionPlayer(ITEM_1_SLOT, event.getPlayer(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player)));
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), "", "§7Vendre §e" + amountToSell, "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * amountToSell));
        }
        if(amountToSell < 16){
            setDescriptionPlayer(ITEM_16_SLOT, event.getPlayer(), "", "§cTu n'as pas assez d'items à vendre");
        } else {
            setDescriptionPlayer(ITEM_16_SLOT, event.getPlayer(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * 16));
        }
        if(amountToSell < 64){
            setDescriptionPlayer(ITEM_64_SLOT, event.getPlayer(), "", "§cTu n'as pas assez d'items à vendre");
        } else {
            setDescriptionPlayer(ITEM_64_SLOT, event.getPlayer(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getPrice(player) * 64));
        }
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
            player.sendMessage("§cTu n'as pas d'item à vendre.");
            player.getPlayer().closeInventory();
            return;
        }
        player.removeSimilarItems(toSell, amount);
        player.addMoney(sellPrice);
        player.sendMessage(Text.PREFIX + "Tu as vendu §e" + toSell.getType().name() + " x" + amount + " §6pour §e" + ConsulatCore.formatMoney(sellPrice * amount));
        onOpened(new GuiOpenEvent(player));
    }
}
