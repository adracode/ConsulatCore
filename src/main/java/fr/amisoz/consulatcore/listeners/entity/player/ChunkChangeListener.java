package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.fly.FlyManager;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
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
public class ChunkChangeListener implements Listener {

    @EventHandler
    public void onChunkChangeEvent(ChunkChangeEvent event) {
        Player player = event.getPlayer();
        Chunk chunkTo = event.getChunkTo();

        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        ClaimObject chunk = ChunkLoader.getClaimedZone(chunkTo);

        if (FlyManager.flyMap.containsKey(player) || FlyManager.infiniteFly.contains(player)) {

            if (!canFly(player, chunk)) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 100));
                player.sendMessage(FlyManager.flyPrefix + "Ton fly est terminé car tu as quitté ton claim !");

                long startFly = FlyManager.flyMap.get(player);

                corePlayer.timeLeft = corePlayer.flyTime - (System.currentTimeMillis() - startFly) / 1000;

                FlyManager.flyMap.remove(player);
                FlyManager.infiniteFly.remove(player);
                CoreManagerPlayers.getCorePlayer(player).lastTime = System.currentTimeMillis();

                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                    try {
                        ConsulatCore.INSTANCE.getFlySQL().saveFly(player, System.currentTimeMillis(), corePlayer.timeLeft);
                    } catch (SQLException e) {
                        player.sendMessage(FlyManager.flyPrefix + "Erreur lors de la sauvegarde du fly.");
                    }
                });
            }
        }
    }

    private boolean canFly(Player player, ClaimObject chunk) {
        if (chunk != null) {
            return chunk.getPlayerUUID().equalsIgnoreCase(player.getUniqueId().toString()) || chunk.access.contains(player.getUniqueId().toString());
        }else return false;
    }
}
