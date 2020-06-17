package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ModeratorInteraction implements Listener {

    @EventHandler
    public void onModeratorInteractWithBlock(BlockPlaceEvent event){
        Player player = event.getPlayer();
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
        if (survivalPlayer.isInModeration() && event.getBlock().getType() == Material.PACKED_ICE){
            player.sendMessage(ChatColor.RED+"Tu ne peux pas poser ce bloc !");
            event.setCancelled(true);
        }
    }
}
