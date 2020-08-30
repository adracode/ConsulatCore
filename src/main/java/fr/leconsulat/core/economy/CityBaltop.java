package fr.leconsulat.core.economy;

import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.cities.City;

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
