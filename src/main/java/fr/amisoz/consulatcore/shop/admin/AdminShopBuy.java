package fr.amisoz.consulatcore.shop.admin;

import fr.amisoz.consulatcore.guis.shop.admin.AdminShopGui;
import fr.amisoz.consulatcore.guis.shop.admin.BuyGui;
import fr.amisoz.consulatcore.utils.CoordinatesUtils;
import org.jetbrains.annotations.NotNull;

public class AdminShopBuy extends AdminShop {
    
    public static final String TYPE = "ADMIN_BUY";
    
    private BuyGui gui;
    
    public AdminShopBuy(int x, int y, int z, double price){
        this(CoordinatesUtils.convertCoordinates(x, y, z), price);
    }
    
    public AdminShopBuy(long coords){
        this(coords, 0);
    }
    
    public AdminShopBuy(long coords, double price){
        super(coords, price);
    }
    
    @Override
    public void createGui(){
        gui = new BuyGui(this);
        gui.onCreate();
    }
    
    @Override
    public @NotNull AdminShopGui getGui(){
        return gui;
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
}
