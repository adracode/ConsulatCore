package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by KIZAFOX on 01/03/2020 for ConsulatCore
 */
public class ModeratorInteraction implements Listener {

    @EventHandler
    public void onModeratorInteractWithBlock(BlockPlaceEvent event){
        Player player = event.getPlayer();
        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);

        if (corePlayer.isModerate() && event.getBlock().getType() == Material.PACKED_ICE){
            player.sendMessage(ChatColor.RED+"Tu ne peux pas poser ce bloc !");
            event.setCancelled(true);
        }
    }
}
