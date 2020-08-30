package fr.leconsulat.core.shop.admin.custom;

import fr.leconsulat.core.guis.shop.admin.AdminShopGui;
import fr.leconsulat.core.guis.shop.admin.custom.CustomGui;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.shop.admin.AdminShop;
import fr.leconsulat.core.utils.CoordinatesUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AdminShopCustom extends AdminShop {
    
    private CustomGui gui;
    
    public AdminShopCustom(int x, int y, int z, double price){
        this(CoordinatesUtils.convertCoordinates(x, y, z), price);
    }
    
    public AdminShopCustom(long coords){
        this(coords, 0);
    }
    
    public AdminShopCustom(long coords, double price){
        super(coords, price);
    }
    
    @Override
    public void createGui(){
        gui = new CustomGui(this, getTitle());
        gui.onCreate();
    }
    
    @Override
    public @NotNull AdminShopGui getGui(){
        return gui;
    }
    
    public abstract String getTitle();
    
    public abstract boolean canBuy(SurvivalPlayer player);
    
    public abstract void onBuy(SurvivalPlayer player);
}
