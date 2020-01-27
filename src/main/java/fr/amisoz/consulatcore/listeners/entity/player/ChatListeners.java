package fr.amisoz.consulatcore.listeners.entity.player;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Calendar;

public class ChatListeners implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        if(!ConsulatCore.chat_activated && PlayersManager.getConsulatPlayer(player).getRank().getRankPower() < RankEnum.ADMIN.getRankPower()){
            player.sendMessage("§cChat coupé.");
            event.setCancelled(true);
        }

        CorePlayer corePlayer = CoreManagerPlayers.getCorePlayer(player);
        if(corePlayer.isMuted){
            if(!(System.currentTimeMillis() >= corePlayer.muteExpireMillis)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(corePlayer.muteExpireMillis);
                String resultDate = ConsulatCore.DATE_FORMAT.format(calendar.getTime());
                String reason = corePlayer.muteReason;
                player.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + reason +"\n§4Jusqu'au : §c" + resultDate);
                event.setCancelled(true);
            }
        }

        RankEnum playerRank = PlayersManager.getConsulatPlayer(player).getRank();
        event.setFormat(playerRank.getRankColor() + "[" + playerRank.getRankName() + "] " + "%s" + ChatColor.GRAY + " : " + ChatColor.WHITE + "%s");
    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] array = event.getMessage().split(" ");
        String command = array[0];

        if(command.equalsIgnoreCase("/msg") || command.equalsIgnoreCase("/whisper") ||command.equalsIgnoreCase("/tell") || command.equalsIgnoreCase("/me") || command.contains("bukkit")){
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cCommande désactivée.");
        }
    }
}
