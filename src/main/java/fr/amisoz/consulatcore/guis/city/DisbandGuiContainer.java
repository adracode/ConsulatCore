package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.module.api.Datable;

public class DisbandGuiContainer extends GuiContainer<City> {
    
    private static DisbandGuiContainer instance;
    
    public DisbandGuiContainer(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
        GuiManager.getInstance().addContainer("city-disband", this);
    }
    
    @Override
    public Datable<City> createGui(City city){
        return new DisbandGui(city);
    }
}
