package fr.amisoz.consulatcore.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChunkChangeEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Chunk from;
    private Chunk to;
    
    public ChunkChangeEvent(Player player, Chunk from, Chunk to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Chunk getChunkFrom() {
        return from;
    }
    
    public Chunk getChunkTo() {
        return to;
    }
}