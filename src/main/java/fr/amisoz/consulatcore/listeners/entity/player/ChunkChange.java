package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.leconsulat.api.claim.ChunkLoader;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.listeners.ChunkChangeEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;

/**
 * Created by KIZAFOX on 13/03/2020 for ConsulatCore
 */
public class ChunkChange implements Listener {

    @EventHandler
    public void onChunkChangeEvent(ChunkChangeEvent event) {
        Player player = event.getPlayer();
        Chunk chunkTo = event.getChunkTo();

        ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(player);
        ClaimObject chunk = ChunkLoader.getClaimedZone(chunkTo);

        //si il leave son claim
        if (chunk == null && consulatPlayer.claimedChunk != null) {
            if(FlyManager.flyMap.containsKey(player) || FlyManager.infiniteFly.contains(player)){
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                player.sendMessage(FlyManager.flyPrefix + "Ton fly est terminé car tu as quitté ton claim !");

                FlyManager.flyMap.remove(player);
                FlyManager.infiniteFly.remove(player);
                CoreManagerPlayers.getCorePlayer(player).lastTime = System.currentTimeMillis();

                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                    try {
                        ConsulatCore.INSTANCE.getFlySQL().setLastTime(player, System.currentTimeMillis());
                    } catch (SQLException e) {
                        player.sendMessage(FlyManager.flyPrefix + "Erreur lors de la sauvegarde du fly.");
                    }
                });
            }
        }
    }
}
