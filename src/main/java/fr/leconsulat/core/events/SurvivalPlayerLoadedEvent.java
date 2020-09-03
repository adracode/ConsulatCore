package fr.leconsulat.core.events;

import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SurvivalPlayerLoadedEvent extends Event {
    
    private static HandlerList handlers = new HandlerList();
    private final SurvivalPlayer player;
    
    public SurvivalPlayerLoadedEvent(SurvivalPlayer player){
        this.player = player;
    }
    
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    
    public SurvivalPlayer getPlayer(){
        return player;
    }
    
    public static HandlerList getHandlerList(){
        return handlers;
    }
    
}
