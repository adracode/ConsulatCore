package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.players.CommandFly;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.runnable.FlyRunnable;
import fr.leconsulat.api.claim.ChunkLoader;
import fr.leconsulat.api.claim.ClaimObject;
import fr.leconsulat.api.listeners.ChunkChangeEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            if(CommandFly.fly5.contains(player) || CommandFly.fly25.contains(player)){
                ConsulatCore.INSTANCE.getFlySQL().setDuration(player, 4);
                player.sendMessage(ChatColor.RED+"Ton fly se terminera dans 4 secondes ! Tu as quitté ton claim..");
                consulatPlayer.claimedChunk = null;
            }else if(CommandFly.infinite.contains(player)){
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10*20, 100));
                ConsulatCore.INSTANCE.getFlySQL().setDuration(player, Integer.MAX_VALUE);
                CommandFly.infinite.remove(player);
                CommandFly.cooldowns.put(player.getName(), System.currentTimeMillis());
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage(ChatColor.RED+"Ton fly est désactivé ! Tu as quitté ton claim..");
                consulatPlayer.claimedChunk = null;
            }
        }
    }
}
