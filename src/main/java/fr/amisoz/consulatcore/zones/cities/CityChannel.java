package fr.amisoz.consulatcore.zones.cities;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.channel.Speakable;
import fr.leconsulat.api.player.ConsulatPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CityChannel extends Channel implements Speakable {
    
    private List<UUID> exclude = new ArrayList<>();
    private UUID[] converter = new UUID[0];
    
    public CityChannel(){
        super("city");
    }
    
    @Override
    public synchronized void sendMessage(String message, UUID... exclude){
        super.sendMessage(message, exclude);
        Channel spy = ConsulatCore.getInstance().getSpy();
        for(ConsulatPlayer member : members){
            if(spy.isMember(member)){
                this.exclude.add(member.getUUID());
            }
        }
        if(this.exclude.isEmpty()){
            ConsulatCore.getInstance().getSpy().sendMessage(message);
        } else {
            ConsulatCore.getInstance().getSpy().sendMessage(message, this.exclude.toArray(converter));
        }
        this.exclude.clear();
    }
    
    @Override
    public String speak(ConsulatPlayer consulatPlayer, String message){
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)consulatPlayer;
        City city = survivalPlayer.getCity();
        CityRank rank = city.getCityPlayer(survivalPlayer.getUUID()).getRank();
        return Text.PREFIX_CITY(city)
                + "§7(" + rank.getColor() + rank.getRankName()
                + "§7) §a" + survivalPlayer.getName() + "§7 > §e" + message;
    }
}
