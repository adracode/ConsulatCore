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
            if(CommandFly.fly.contains(player)){
                ConsulatCore.INSTANCE.getFlySQL().setDuration(player, 4);
                player.sendMessage(ChatColor.RED+"Ton fly se terminera dans 4 secondes ! Tu as quitté ton claim..");
                consulatPlayer.claimedChunk = null;
            }
        }
    }
}
