package fr.amisoz.consulatcore.guis.shop.admin;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.shop.admin.AdminShop;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import org.bukkit.inventory.ItemStack;

public class BuyGui extends AdminShopGui {
    
    public BuyGui(AdminShop data){
        super(data, "Acheter");
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        GuiItem buy1 = IGui.getItem("§eAcheter 1", ITEM_1_SLOT, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice()));
        GuiItem buy16 = IGui.getItem("§eAcheter 16", ITEM_16_SLOT, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice() * 16));
        buy16.setAmount(16);
        GuiItem buy64 = IGui.getItem("§eAcheter 64", ITEM_64_SLOT, getData().getItem().getType(), "", "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice() * 64));
        buy64.setAmount(64);
        GuiItem fillInventory = IGui.getItem("§eRemplir l'inventaire", ITEM_ALL_SLOT, getData().getItem().getType());
        fillInventory.setAmount(64);
        setItem(buy1);
        setItem(buy16);
        setItem(buy64);
        setItem(fillInventory);
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        int spaceAvailable = ((SurvivalPlayer)event.getPlayer()).spaceAvailable(getData().getItem());
        if(spaceAvailable == 0){
            setDescriptionPlayer(ITEM_1_SLOT, event.getPlayer(), "", "§cTu n'as pas assez de", "§cplace dans ton inventaire");
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), "", "§cTon inventaire est plein");
        } else {
            setDescriptionPlayer(ITEM_ALL_SLOT, event.getPlayer(), "", "§7Acheter §e" + spaceAvailable, "§7Prix: §e" + ConsulatCore.formatMoney(getData().getPrice() * spaceAvailable));
        }
        if(spaceAvailable < 16){
            setDescriptionPlayer(ITEM_16_SLOT, event.getPlayer(), "", "§cTu n'as pas assez de", "§cplace dans ton inventaire");
        }
        if(spaceAvailable < 64){
            setDescriptionPlayer(ITEM_64_SLOT, event.getPlayer(), "", "§cTu n'as pas assez de", "§cplace dans ton inventaire");
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        int amount = -1;
        switch(event.getSlot()){
            case ITEM_1_SLOT:
                amount = 1;
                break;
            case ITEM_16_SLOT:
                amount = 16;
                break;
            case ITEM_64_SLOT:
                amount = 64;
                break;
            case ITEM_ALL_SLOT:
                break;
            default:
                return;
        }
        AdminShop shop = getData();
        ItemStack toBuy = shop.getItem().clone();
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        int spaceAvailable = player.spaceAvailable(toBuy);
        if(amount == -1){
            amount = spaceAvailable;
        }
        if(spaceAvailable < amount || spaceAvailable == 0){
            player.sendMessage("§cTu n'as pas assez de place dans ton inventaire.");
            player.getPlayer().closeInventory();
            return;
        }
        double buyPrice = amount * shop.getPrice();
        if(player.hasMoney(buyPrice)){
            player.removeMoney(buyPrice);
            player.sendMessage(Text.PREFIX + "Tu as acheté §e" + shop.getItem().getType().name() + " x" + amount + " §6pour §e" + ConsulatCore.formatMoney(buyPrice) + ".");
            player.addItemInInventory(amount, toBuy);
            onOpened(new GuiOpenEvent(player));
        } else {
            player.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent");
            player.getPlayer().closeInventory();
        }
    }
}
