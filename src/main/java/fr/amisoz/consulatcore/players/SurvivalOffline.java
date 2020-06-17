package fr.amisoz.consulatcore.players;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.ranks.Rank;

import java.util.UUID;

public class SurvivalOffline extends ConsulatOffline  {

    private double money;
    private City city;

    public SurvivalOffline(int id, UUID uuid, String name, Rank rank, String registered, double money, City city){
        super(id, uuid, name, rank, registered);
        this.money = money;
        this.city = city;
    }

    public double getMoney(){
        return money;
    }

    public City getCity(){
        return city;
    }
}
