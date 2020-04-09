package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.claim.ChunkLoader;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.listeners.ChunkChangeEvent;
import fr.leconsulat.api.player.PlayersManager;
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
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if(corePlayer.isFreezed){
            event.setCancelled(true);
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if((from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch())){
            corePlayer.lastMove = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(event.getPlayer());
        corePlayer.oldLocation = event.getFrom();
        corePlayer.lastTeleport = System.currentTimeMillis();

        Chunk chunkTo = event.getTo().getChunk();
        Chunk chunkFrom = event.getFrom().getChunk();

        Bukkit.getPluginManager().callEvent(new ChunkChangeEvent(event.getPlayer(), chunkFrom, chunkTo));
    }

}
