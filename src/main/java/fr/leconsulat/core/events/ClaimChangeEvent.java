package fr.leconsulat.core.events;

import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClaimChangeEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private @NotNull SurvivalPlayer player;
    private @Nullable Claim from;
    private @Nullable Claim to;
    
    public ClaimChangeEvent(@NotNull SurvivalPlayer player, @Nullable Claim from, @Nullable Claim to){
        this.player = player;
        this.from = from;
        this.to = to;
    }
    
    @NotNull
    public HandlerList getHandlers(){
        return handlers;
    }
    
    public @NotNull SurvivalPlayer getPlayer(){
        return player;
    }
    
    public @Nullable Claim getClaimFrom(){
        return from;
    }
    
    public @Nullable Claim getClaimTo(){
        return to;
    }
    
    public static HandlerList getHandlerList(){
        return handlers;
    }
}