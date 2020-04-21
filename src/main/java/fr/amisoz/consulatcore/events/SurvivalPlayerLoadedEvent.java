package fr.amisoz.consulatcore.events;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SurvivalPlayerLoadedEvent extends Event {
    
    private final SurvivalPlayer player;
    
    public SurvivalPlayerLoadedEvent(SurvivalPlayer player){
        this.player = player;
    }
    
    public SurvivalPlayer getPlayer(){
        return player;
    }
    
    private static HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlerList(){
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    
}
