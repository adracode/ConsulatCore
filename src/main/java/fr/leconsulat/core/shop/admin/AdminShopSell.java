package fr.leconsulat.core.shop.admin;

import fr.leconsulat.core.guis.shop.admin.AdminShopGui;
import fr.leconsulat.core.guis.shop.admin.SellGui;
import fr.leconsulat.core.utils.CoordinatesUtils;
import org.jetbrains.annotations.NotNull;

public class AdminShopSell extends AdminShop {
    
    public static final String TYPE = "ADMIN_SELL";
    
    private SellGui gui;
    
    public AdminShopSell(int x, int y, int z, double price){
        this(CoordinatesUtils.convertCoordinates(x, y, z), price);
    }
    
    public AdminShopSell(long coords){
        this(coords, 0);
    }
    
    public AdminShopSell(long coords, double price){
        super(coords, price);
    }
    
    @Override
    public void createGui(){
        gui = new SellGui(this);
        gui.onCreate();
    }
    
    @Override
    public @NotNull AdminShopGui getGui(){
        if(gui == null){
            throw new NullPointerException("SellGui");
        }
        return gui;
    }
    
    @Override
    public String getType(){
        return TYPE;
    }
}
