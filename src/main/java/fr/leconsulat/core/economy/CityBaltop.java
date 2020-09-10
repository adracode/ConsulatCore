package fr.leconsulat.core.economy;

import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.cities.City;

import java.util.ArrayList;
import java.util.Collection;

public class CityBaltop extends Baltop<City> {
    
    public CityBaltop(){
        super(5, City::getMoney);
    }
 
    @Override
    public Collection<City> getMoneyOwners(){
        ArrayList<City> cities = new ArrayList<>(ZoneManager.getInstance().getCities());
        cities.removeIf(City::isNoDamage);
        return cities;
    }
}
