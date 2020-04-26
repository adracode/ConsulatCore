package fr.amisoz.consulatcore.players;

import fr.leconsulat.api.player.ConsulatOffline;
import fr.leconsulat.api.ranks.Rank;

import java.util.UUID;

public class SurvivalOffline extends ConsulatOffline  {
    
    private double money;
    
    public SurvivalOffline(int id, UUID uuid, String name, Rank rank, String registered, double money){
        super(id, uuid, name, rank, registered);
        this.money = money;
    }
    
    public double getMoney(){
        return money;
    }
    
}
