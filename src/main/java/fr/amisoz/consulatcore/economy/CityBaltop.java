package fr.amisoz.consulatcore.economy;

import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;

import java.util.Collection;

public class CityBaltop extends Baltop<City> {
    
    public CityBaltop(){
        super(5, City::getMoney);
    }
 
    @Override
    public Collection<City> getMoneyOwners(){
        return ZoneManager.getInstance().getCities();
    }
}
