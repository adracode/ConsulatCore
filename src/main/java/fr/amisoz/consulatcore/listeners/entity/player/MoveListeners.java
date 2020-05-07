package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.events.ChunkChangeEvent;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MoveListeners implements Listener {
    
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if(survivalPlayer.isFrozen()){
            event.setCancelled(true);
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if(from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()){
            survivalPlayer.setLastMove(System.currentTimeMillis());
        }
    }
    
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player == null){
            return;
        }
        player.setOldLocation(event.getFrom());
        player.setLastTeleport(System.currentTimeMillis());
        Chunk chunkTo = event.getTo().getChunk();
        Chunk chunkFrom = event.getFrom().getChunk();
        Bukkit.getPluginManager().callEvent(new ChunkChangeEvent(event.getPlayer(), chunkFrom, chunkTo));
    }
    
}
