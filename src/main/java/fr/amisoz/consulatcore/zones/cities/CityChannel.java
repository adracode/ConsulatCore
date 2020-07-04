package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.player.ConsulatPlayer;

public class CityChannel extends Channel {
    
    public CityChannel(){
        super("city-channel");
    }
    
    @Override
    public String format(ConsulatPlayer player, String message){
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)player;
        return "§8[§d" + survivalPlayer.getCity().getName() + "§8] "
                + "§7(§d" + survivalPlayer.getCity().getCityPlayer(survivalPlayer.getUUID()).getRank().getRankName()
                + "§7) §a" + survivalPlayer.getName() + "§7 > §e" + message;
        
    }
}
