package fr.amisoz.consulatcore.guis.shop;

import fr.amisoz.consulatcore.shop.ShopItemType;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.module.api.Datable;

public class ShopGuiContainer extends GuiContainer<ShopItemType> {
    
    private static ShopGuiContainer instance;
    
    public ShopGuiContainer(){
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
