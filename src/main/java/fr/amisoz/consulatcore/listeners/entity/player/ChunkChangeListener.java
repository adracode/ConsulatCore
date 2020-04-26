package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.events.ChunkChangeEvent;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

public class ChunkChangeListener implements Listener {
    
    @EventHandler
    public void onChunkChangeEvent(ChunkChangeEvent event){
        SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(event.getPlayer().getUniqueId());
        if(player.isFlying()){
            if(!player.canFly(event.getChunkTo())){
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
