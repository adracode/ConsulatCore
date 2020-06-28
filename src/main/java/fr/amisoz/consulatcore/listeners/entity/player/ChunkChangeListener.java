package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.events.ClaimChangeEvent;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;

public class ChunkChangeListener implements Listener {
    
    @EventHandler
    public void onChunkChangeEvent(ClaimChangeEvent event){
        SurvivalPlayer player = event.getPlayer();
        if(player.isFlying()){
            if(!player.canFlyHere(event.getClaimTo())){
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        player.disableFly();
                        player.sendMessage(Text.FLY + "Ton fly est terminé car tu as quitté ton claim !");
                    } catch(SQLException e){
                        player.sendMessage(Text.FLY + "Erreur lors de la sauvegarde du fly.");
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
