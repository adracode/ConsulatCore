package fr.leconsulat.core.guis.shop.admin.custom;

import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.guis.shop.admin.AdminShopGui;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.shop.admin.custom.AdminShopCustom;

public class CustomGui extends AdminShopGui {
    
    private static final byte BUY_SLOT = 22;
    
    public CustomGui(AdminShopCustom data, String title){
        super(data, title);
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        moveItem(INFO_SLOT, BUY_SLOT);
    }
    
    @Override
    public void onOpened(GuiOpenEvent event){
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        if(!getData().canBuy(player)){
            setDescriptionPlayer(BUY_SLOT, player, GuiItem.getDescription(getItem(BUY_SLOT), "", "§cTu ne peux pas acheter"));
        } else if(!player.hasMoney(getData().getPrice())){
            setDescriptionPlayer(BUY_SLOT, player, GuiItem.getDescription(getItem(BUY_SLOT), "", "§cTu n'as pas assez d'argent"));
        } else {
            removeFakeItem(BUY_SLOT, player);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        if(event.getSlot() != BUY_SLOT){
            return;
        }
        SurvivalPlayer player = (SurvivalPlayer)event.getPlayer();
        AdminShopCustom adminShopCustom = getData();
        if(!adminShopCustom.canBuy(player)){
            return;
        }
        double price = adminShopCustom.getPrice();
        if(!player.hasMoney(price)){
            player.sendActionBar(Text.NOT_ENOUGH_MONEY(price));
            return;
        }
        player.removeMoney(price);
        adminShopCustom.onBuy(player);
        refresh(player);
    }
    
    @Override
    public AdminShopCustom getData(){
        return (AdminShopCustom)super.getData();
    }
}
